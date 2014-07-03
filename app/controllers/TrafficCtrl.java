package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import models.traffic.plane.AirRoute;
import models.traffic.plane.FlightPrice;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.Traffic;
import utils.Utils;

import java.net.UnknownHostException;
import java.sql.Time;
import java.text.DateFormat;
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
     * @param flightCode
     * @return
     */
    public static Result getAirRouteByCode(String flightCode) throws UnknownHostException {
        if (flightCode == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid flight code.");

        flightCode = flightCode.toUpperCase();
        DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("air_route");
        DBObject flight = col.findOne(QueryBuilder.start("code").is(flightCode).get());
        if (flight == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid flight code: %s.", flightCode));

        flight.put("_id", flight.get("_id").toString());
        for (String k : new String[]{"arrAirport", "arr", "depAirport", "dep", "carrier"}) {
            DBObject tmp = (DBObject) flight.get(k);
            tmp.put("_id", tmp.get("_id").toString());
        }

        final DateFormat fmt = new SimpleDateFormat("HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        fmt.setTimeZone(tz);
        for (String k : new String[]{"depTime", "arrTime"})
            flight.put(k, fmt.format((Date) flight.get(k)));

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(flight));
    }

    /**
     * 获得航班信息
     *
     * @param depId      出发地id
     * @param arrId      到达地id
     * @param sort       排序方式
     * @param timeFilter 出发时间过滤
     * @param page       分页偏移量
     * @param pageSize   页面大小
     * @return 航班列表
     */
    public static Result getAirRoutes(String depId, String arrId, String sortField, String sort,
                                      String timeFilterType, int timeFilter, int page, int pageSize)
            throws TravelPiException {
        Traffic.SortField sf = null;
        switch (sortField) {
            case "price":
                sf = Traffic.SortField.PRICE;
                break;
            case "dep":
                sf = Traffic.SortField.DEP_TIME;
                break;
            case "arr":
                sf = Traffic.SortField.ARR_TIME;
                break;
            case "timeCost":
                sf = Traffic.SortField.TIME_COST;
                break;
        }
        Traffic.SortType st = null;
        switch (sort) {
            case "asc":
                st = Traffic.SortType.ASC;
                break;
            case "desc":
                st = Traffic.SortType.DESC;
                break;
        }

        // 时间段过滤
        List<Calendar> timeLimits = null;
        switch (timeFilter) {
            case 0:
                break;
            case 1:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 6, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 12, 0);
                    add(cal);
                }};
                break;
            case 2:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 12, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 18, 0);
                    add(cal);
                }};
                break;
            case 3:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 18, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 23, 59);
                    add(cal);
                }};
                break;
        }
        List<Calendar> depLimits = null;
        List<Calendar> arrLimits = null;
        if (timeFilterType.equals("dep"))
            depLimits = timeLimits;
        else if (timeFilterType.equals("arr"))
            arrLimits = timeLimits;

        BasicDBList routeList = Traffic.searchAirRoutes(depId, arrId, null, depLimits, arrLimits, null, sf, st, page, pageSize);
        for (Object tmp : routeList) {
            DBObject flight = (DBObject) tmp;
            flight.put("_id", flight.get("_id").toString());
            for (String k : new String[]{"arrAirport", "arr", "depAirport", "dep", "carrier"}) {
                DBObject tmpNode = (DBObject) flight.get(k);
                tmpNode.put("_id", tmpNode.get("_id").toString());
            }

            final DateFormat fmt = new SimpleDateFormat("HH:mm");
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            fmt.setTimeZone(tz);
            for (String k : new String[]{"depTime", "arrTime"})
                flight.put(k, fmt.format((Date) flight.get(k)));
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(routeList));
    }

    /**
     * 查看火车信息
     *
     * @param departure
     * @param arrival
     * @param sortField
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getTrainRoutes(String departure, String arrival, String sortField, String sort,
                                        String timeFilterType, int timeFilter, int page, int pageSize)
            throws UnknownHostException, TravelPiException {
        Traffic.SortField sf = null;
        switch (sortField) {
            case "price":
                sf = Traffic.SortField.PRICE;
                break;
            case "dep":
                sf = Traffic.SortField.DEP_TIME;
                break;
            case "arr":
                sf = Traffic.SortField.ARR_TIME;
                break;
            case "timeCost":
                sf = Traffic.SortField.TIME_COST;
                break;
        }
        Traffic.SortType st = null;
        switch (sort) {
            case "asc":
                st = Traffic.SortType.ASC;
                break;
            case "desc":
                st = Traffic.SortType.DESC;
                break;
        }

        // 时间段过滤
        List<Calendar> timeLimits = null;
        switch (timeFilter) {
            case 0:
                break;
            case 1:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 6, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 12, 0);
                    add(cal);
                }};
                break;
            case 2:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 12, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 18, 0);
                    add(cal);
                }};
                break;
            case 3:
                timeLimits = new ArrayList<Calendar>() {{
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 18, 0);
                    add(cal);
                    cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                    cal.set(1980, Calendar.JANUARY, 1, 23, 59);
                    add(cal);
                }};
                break;
        }
        List<Calendar> depLimits = null;
        List<Calendar> arrLimits = null;
        if (timeFilterType.equals("dep"))
            depLimits = timeLimits;
        else if (timeFilterType.equals("arr"))
            arrLimits = timeLimits;

        BasicDBList routeList = Traffic.searchTrainRoute(departure, arrival, null, depLimits, arrLimits, null, null, sf, st);

        int fromIdx = page * pageSize;
        if (fromIdx >= routeList.size())
            fromIdx = routeList.size() - 1;
        int toIdx = fromIdx + pageSize;
        if (toIdx > routeList.size())
            toIdx = routeList.size();
        List<Object> ret = routeList.subList(fromIdx, toIdx);

        final DateFormat fmt = new SimpleDateFormat("HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        fmt.setTimeZone(tz);
        for (Object obj : ret) {
            DBObject route = (DBObject) obj;
            route.put("_id", route.get("_id").toString());
            for (String k1 : new String[]{"arr", "dep"}) {
                DBObject stop = (DBObject) route.get(k1);
                for (String k2 : new String[]{"locId", "stopId"}) {
                    stop.put(k2, stop.get(k2).toString());
                }
            }
            route.put("depTime", fmt.format(route.get("depTime")));
            route.put("arrTime", fmt.format(route.get("arrTime")));
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(ret));
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
