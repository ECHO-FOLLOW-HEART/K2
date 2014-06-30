package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import models.traffic.plane.AirRoute;
import models.traffic.plane.FlightPrice;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.Utils;

import java.net.UnknownHostException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 交通相关
 *
 * @author Zephyre
 */
public class TrafficCtrl extends Controller {

    /**
     * 按照航班号获得航班信息。
     *
     * @param flightCode 航班号。
     * @return 航班信息。
     */
    public static Result getAirRouteByCode(String flightCode) {
        AirRoute route = AirRoute.finder.byId(flightCode);
        if (route == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT,
                    String.format("Invalid flight code: %s", flightCode));
        return Utils.createResponse(ErrorCode.NORMAL, getAirRouteNode(route));
    }

    /**
     * 获得航班信息
     *
     * @param departure  出发地id
     * @param arrival    到达地id
     * @param sort       排序方式
     * @param timeFilter 出发时间过滤
     * @param page       分页偏移量
     * @param pageSize   页面大小
     * @return 航班列表
     */
    public static Result getAirRoutes(long departure, long arrival, String sortType, String sort,
                                      String timeFilterType, int timeFilter, int page, int pageSize) {
        ExpressionList<AirRoute> expList = AirRoute.finder.fetch("airline").fetch("priceList")
                .fetch("departure").fetch("departure.locality")
                .fetch("arrival").fetch("arrival.locality")
                .where().eq("departure.locality.id", departure).eq("arrival.locality.id", arrival);

        String filterField = (timeFilterType.equals("arr") ? "arrivalTime" : "departureTime");
        if (timeFilter == 1)
            expList = expList.ge(filterField, Time.valueOf("06:00:00")).le(filterField, Time.valueOf("12:00:00"));
        else if (timeFilter == 2)
            expList = expList.ge(filterField, Time.valueOf("12:00:00")).le(filterField, Time.valueOf("18:00:00"));
        else if (timeFilter == 3)
            expList = expList.disjunction().add(Expr.ge(filterField, Time.valueOf("18:00:00")))
                    .add(Expr.le(filterField, Time.valueOf("06:00:00")));

        Query<AirRoute> query;
        String field;
        switch (sortType) {
            case "dep":
                field = "departureTime";
                break;
            case "arr":
                field = "arrivalTime";
                break;
            case "price":
            default:
                field = "price";
                break;
        }

        if (sort.equals("dsc"))
            query = expList.order().desc(field);
        else
            query = expList.order().asc(field);

        List<AirRoute> routeList = query.setFirstRow(page * pageSize).setMaxRows(pageSize).findList();

        List<JsonNode> result = new ArrayList<>();
        for (AirRoute route : routeList)
            result.add(getAirRouteNode(route));

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
    }

    private static ObjectNode getAirRouteNode(AirRoute route) {
        ObjectNode routeJson = Json.newObject();
        routeJson.put("flightCode", route.flightCode);
        routeJson.put("airlineName", route.airline.airlineFullName);
        routeJson.put("departureAirport", route.departure.name);
        routeJson.put("arrivalAirport", route.arrival.name);
        String terminalStr = route.departureTerminal;
        if (terminalStr != null && !terminalStr.isEmpty())
            routeJson.put("departureTerminal", terminalStr);
        terminalStr = route.arrivalTerminal;
        if (terminalStr != null && !terminalStr.isEmpty())
            routeJson.put("arrivalTerminal", terminalStr);
        routeJson.put("departureLocality", route.departure.locality.localLocalityName);
        routeJson.put("arrivalLocality", route.arrival.locality.localLocalityName);
        routeJson.put("distance", route.distance);

        routeJson.put("airlineCode", route.airline.airlineCode);
        routeJson.put("departureTime", route.departureTime.toString());
        routeJson.put("arrivalTime", route.arrivalTime.toString());
        routeJson.put("dayLag", route.dayLag);
        routeJson.put("duration", route.duration);
        Float onTime = route.onTimeStat;
        if (onTime > 0)
            routeJson.put("onTimeRate", (int) (onTime * 100) / 100.0);
        if (!route.priceList.isEmpty()) {
            FlightPrice price = route.priceList.get(0);
            routeJson.put("price", price.ticketPrice / 100.0);
            routeJson.put("discount", (int) (price.discount * 100) / 100.0);
            routeJson.put("tax", price.tax / 100.0);
            routeJson.put("fuelSurcharge", price.fuelSurcharge / 100.0);
        }
        return routeJson;
    }

    /**
     * 查看火车信息
     *
     * @param departure
     * @param arrival
     * @param sortType
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getTrainRoutes(String departure, String arrival, String sortType, String sort, int page, int pageSize) throws UnknownHostException {
        MongoClient client = Utils.getMongoClient();
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("train_route");

        ObjectId dep, arr;
        try {
            dep = new ObjectId(departure);
            arr = new ObjectId(arrival);
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s / %s", departure, arrival));
        }

        QueryBuilder query1 = new QueryBuilder();
        query1.or(QueryBuilder.start("details.locId").is(dep).get(), QueryBuilder.start("details.stopId").is(dep).get()).get();
        QueryBuilder query2 = new QueryBuilder();
        query2.or(QueryBuilder.start("details.locId").is(arr).get(), QueryBuilder.start("details.stopId").is(arr).get()).get();
        QueryBuilder qb = new QueryBuilder();
        qb.and(query1.get(), query2.get());
        DBObject query = qb.get();

        DBCursor cursor = col.find(query);
        List<JsonNode> result = new ArrayList<>();
        final DateFormat fmt = new SimpleDateFormat("HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        fmt.setTimeZone(tz);
        while (cursor.hasNext()) {
            DBObject route = cursor.next();
            BasicDBList details = (BasicDBList) (route.get("details"));
            route.removeField("details");

            // 到达节点必须晚于出发节点
            int depIdx = -1;
            int arrIdx = -1;
            BasicDBObject arrStop = null;
            BasicDBObject depStop = null;
            for (int i = 0; i < details.size(); i++) {
                BasicDBObject stop = (BasicDBObject) details.get(i);
                if (stop.get("stopId").equals(dep) || stop.get("locId").equals(dep)) {
                    depIdx = i;
                    depStop = stop;
                } else if (stop.get("stopId").equals(arr) || stop.get("locId").equals(arr)) {
                    arrIdx = i;
                    arrStop = stop;
                }

                if (arrIdx != -1 && depIdx == -1)
                    // 出现先到达再出发的情况：反向车次
                    break;
                else if (arrIdx != -1 && depIdx != -1)
                    break;
            }
            if (!(arrIdx != -1 && depIdx != -1))
                continue;

            // 建立节点
            ObjectNode jsonItem = Json.newObject();

            int dayLag = (int) arrStop.get("dayLag") - (int) depStop.get("dayLag");
            jsonItem.put("dayLag", dayLag);

            ObjectNode depNode = Json.newObject();
            depNode.put("locId", depStop.get("locId").toString());
            depNode.put("locName", depStop.get("locName").toString());
            depNode.put("stopId", depStop.get("stopId").toString());
            depNode.put("stopName", depStop.get("stopName").toString());
            jsonItem.put("dep", depNode);

            ObjectNode arrNode = Json.newObject();
            arrNode.put("locId", arrStop.get("locId").toString());
            arrNode.put("locName", arrStop.get("locName").toString());
            arrNode.put("stopId", arrStop.get("stopId").toString());
            arrNode.put("stopName", arrStop.get("stopName").toString());
            jsonItem.put("arr", arrNode);

            Date depTime = (Date) depStop.get("depTime");
            Date arrTime = (Date) arrStop.get("arrTime");
            jsonItem.put("depTime", fmt.format(depTime));
            jsonItem.put("arrTime", fmt.format(arrTime));
            jsonItem.put("timeCost", (int) ((arrTime.getTime() + 24 * 3600 * 1000L * dayLag - depTime.getTime()) / (60 * 1000)));

            // 路程
            jsonItem.put("totalDist", (int) route.get("distance"));
            jsonItem.put("distance", ((int) arrStop.get("distance") - (int) depStop.get("distance")));

            // 票价
            double minArrPrice = Double.MAX_VALUE, minDepPrice = Double.MAX_VALUE;
            BasicDBObject priceList = (BasicDBObject) arrStop.get("price");
            if (priceList != null) {
                Object[] prices = priceList.values().toArray();
                for (Object price1 : prices) {
                    double price = (double) price1;
                    if (price < minArrPrice)
                        minArrPrice = price;
                }
            }
            if (minArrPrice == Double.MAX_VALUE)
                minArrPrice = 0;
            priceList = (BasicDBObject) depStop.get("price");
            if (priceList != null) {
                Object[] prices = priceList.values().toArray();
                for (Object price1 : prices) {
                    double price = (double) price1;
                    if (price < minDepPrice)
                        minDepPrice = price;
                }
            }
            if (minDepPrice == Double.MAX_VALUE)
                minDepPrice = 0;
            jsonItem.put("price", minArrPrice - minDepPrice);

            jsonItem.put("_id", route.get("_id").toString());
            jsonItem.put("code", route.get("code").toString());
            jsonItem.put("type", route.get("type").toString());

            result.add(jsonItem);
        }

        if (result.isEmpty())
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));

        // 排序
        final boolean asc = (sort.equals("asc"));
        Comparator<JsonNode> cmp;
        if (sortType.equals("price")) {
            cmp = new Comparator<JsonNode>() {
                @Override
                public int compare(JsonNode o1, JsonNode o2) {
                    double price1 = o1.get("price").asDouble();
                    double price2 = o2.get("price").asDouble();
                    int val = (int) (price1 - price2);
                    return (asc ? val : -val);
                }
            };
        } else if (sortType.equals("timeCost")) {
            cmp = new Comparator<JsonNode>() {
                @Override
                public int compare(JsonNode o1, JsonNode o2) {
                    int t1 = o1.get("timeCost").asInt();
                    int t2 = o2.get("timeCost").asInt();
                    int val = t1 - t2;
                    return (asc ? val : -val);
                }
            };
        } else {
            final String s = sortType;
            cmp = new Comparator<JsonNode>() {
                @Override
                public int compare(JsonNode o1, JsonNode o2) {
                    String k;
                    if (s.equals("dep"))
                        k = "depTime";
                    else
                        k = "arrTime";
                    try {
                        Date time1 = fmt.parse(o1.get(k).asText());
                        Date time2 = fmt.parse(o2.get(k).asText());
                        int val = (int) (time1.getTime() - time2.getTime());
                        return (asc ? val : -val);
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            };
        }
        Collections.sort(result, cmp);
        int start = page * pageSize;
        start = (start < result.size() ? start : result.size() - 1);
        int to = (page + 1) * pageSize;
        to = (to <= result.size() ? to : result.size());
        result = result.subList(start, to);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
    }

    /**
     * 按照车次获得火车信息。
     *
     * @param trainCode
     * @return
     * @throws UnknownHostException
     */
    public static Result getTrainRouteByCode(String trainCode) throws UnknownHostException {
        trainCode = trainCode.toUpperCase();
        MongoClient client = Utils.getMongoClient();
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("train_route");

        DBObject query = QueryBuilder.start("code").is(trainCode).get();
        DBObject route = col.findOne(query);
        if (route == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid train code: %s.", trainCode));

        ObjectNode ret = Json.newObject();
        ret.put("_id", route.get("_id").toString());
        ret.put("distance", (int) route.get("distance"));
        ret.put("code", route.get("code").toString());
        ret.put("timeCost", (int) route.get("timeCost"));

        int dayLag = (int) route.get("dayLag");
        ret.put("dayLag", dayLag);
        DateFormat fmt = new SimpleDateFormat("HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        fmt.setTimeZone(tz);

        Date depTime = (Date) route.get("depTime");
        Date arrTime = (Date) route.get("arrTime");

        ret.put("arrTime", fmt.format(arrTime));
        ret.put("depTime", fmt.format(depTime));

        DBObject depStop = (DBObject) route.get("depStop");
        DBObject arrStop = (DBObject) route.get("arrStop");
        DBObject depLoc = (DBObject) route.get("dep");
        DBObject arrLoc = (DBObject) route.get("arr");

        ObjectNode depNode = Json.newObject();
        depNode.put("locId", depLoc.get("_id").toString());
        depNode.put("locName", depLoc.get("name").toString());
        depNode.put("stopId", depStop.get("_id").toString());
        depNode.put("stopName", depStop.get("name").toString());

        ObjectNode arrNode = Json.newObject();
        arrNode.put("locId", arrLoc.get("_id").toString());
        arrNode.put("locName", arrLoc.get("name").toString());
        arrNode.put("stopId", arrStop.get("_id").toString());
        arrNode.put("stopName", arrStop.get("name").toString());

        ret.put("dep", depNode);
        ret.put("arr", arrNode);

        ret.put("type", route.get("type").toString());

        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }


    /**
     * 按照城市搜索火车站
     *
     * @param id
     * @return
     */
    public static Result searchTrainStationByLoc(String id) throws UnknownHostException {
        MongoClient client = Utils.getMongoClient();
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("train_route");

//        QueryBuilder.start("locId").is(new ObjectId(id))

        return Results.TODO;
    }
}
