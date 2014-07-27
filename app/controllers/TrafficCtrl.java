package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.TrafficAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.RouteIterator;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.Traffic;
import utils.Utils;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 交通相关
 *
 * @author Zephyre
 */
public class TrafficCtrl extends Controller {

    /**
     * 按照航班号获得航班信息。
     *
     * @param flightCode 航班号
     * @param ts         出发日期。格式为"yyyy-MM-dd"。如果为null，或""，自动采用下一天作为出发时间。
     * @return
     */
    public static Result getAirRouteByCode(String flightCode, String ts) {
        Calendar cal = null;

        if (ts != null && !ts.isEmpty()) {
            Matcher matcher = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}").matcher(ts);
            if (matcher.find()) {
                ts = matcher.group();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = fmt.parse(ts);
                    cal = Calendar.getInstance();
                    cal.setTime(date);
                } catch (ParseException ignored) {
                }
            }

        }

        if (cal == null) {
            // 默认：一天以后
            cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() + 24 * 3600 * 1000);
        }

        try {
            AirRoute route = TrafficAPI.getAirRouteByCode(flightCode, cal);
            if (route == null)
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid flight code: %s.",
                        flightCode != null ? flightCode : "NULL"));
            return Utils.createResponse(ErrorCode.NORMAL, route.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }


    /**
     * 获得航班信息。
     *
     * @param depId      出发地id（机场、城市均可）。
     * @param arrId      到达地id（机场、城市均可）。
     * @param ts         出发时间。
     * @param sortType       排序方式。
     * @param timeFilter 出发时间过滤。dep：按照出发时间过滤；arr：按照到达时间过滤。
     * @param page       分页偏移量。
     * @param pageSize   页面大小。
     * @return 航班列表
     */
    public static Result searchAirRoutes(String depId, String arrId, String ts, String sortField, String sortType, String timeFilterType, int timeFilter, int page, int pageSize)
            throws TravelPiException {
        int sort = -1;
        if (sortType != null && sortType.equals("asc"))
            sort = 1;

        Calendar cal = null;
        if (ts != null && !ts.isEmpty()) {
            Matcher matcher = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}").matcher(ts);
            if (matcher.find()) {
                ts = matcher.group();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = fmt.parse(ts);
                    cal = Calendar.getInstance();
                    cal.setTime(date);
                } catch (ParseException ignored) {
                }
            }

        }
        if (cal == null) {
            // 默认：一天以后
            cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() + 24 * 3600 * 1000);
        }

        TrafficAPI.SortField sf = TrafficAPI.SortField.PRICE;
        switch (sortField) {
            case "price":
                sf = TrafficAPI.SortField.PRICE;
                break;
            case "dep":
                sf = TrafficAPI.SortField.DEP_TIME;
                break;
            case "arr":
                sf = TrafficAPI.SortField.ARR_TIME;
                break;
            case "timeCost":
                sf = TrafficAPI.SortField.TIME_COST;
                break;
        }

        // 时间段过滤
        Calendar lower = Calendar.getInstance();
        Calendar upper = Calendar.getInstance();
        List<Calendar> timeLimits = null;
        switch (timeFilter) {
            case 1:
                lower.set(Calendar.HOUR_OF_DAY, 6);
                upper.set(Calendar.HOUR_OF_DAY, 12);
                break;
            case 2:
                lower.set(Calendar.HOUR_OF_DAY, 6);
                upper.set(Calendar.HOUR_OF_DAY, 12);
                break;
            case 3:
                lower.set(Calendar.HOUR_OF_DAY, 12);
                upper.set(Calendar.HOUR_OF_DAY, 18);
                break;
            case 4:
                lower.set(Calendar.HOUR_OF_DAY, 18);
                upper.set(Calendar.HOUR_OF_DAY, 23);
                upper.set(Calendar.MINUTE, 59);
                upper.set(Calendar.SECOND, 59);
                upper.set(Calendar.MILLISECOND, 0);
                break;
            default:
                lower = null;
                upper = null;
        }
        if (lower != null && upper != null)
            timeLimits = Arrays.asList(lower, upper);
        List<Calendar> depLimits = null;
        List<Calendar> arrLimits = null;
        if (timeFilterType.equals("dep"))
            depLimits = timeLimits;
        else if (timeFilterType.equals("arr"))
            arrLimits = timeLimits;

        try {
            ObjectId depOid = new ObjectId(depId);
            ObjectId arrOid = new ObjectId(arrId);

            List<JsonNode> results = new ArrayList<>();
            for (RouteIterator it = TrafficAPI.searchAirRoutes(depOid, arrOid, cal, depLimits, arrLimits, null, null,
                    sf, sort, page, pageSize); it.hasNext(); ) {
                results.add(it.next().toJson());
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid IDs. Dep: %s, arr: %s.",
                    (depId != null ? depId : "NULL"), (arrId != null ? arrId : "NULL")));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

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

        List<Object> ret;
        int fromIdx = page * pageSize;
        if (fromIdx >= routeList.size())
            ret = new ArrayList<>();
        else {
            int toIdx = fromIdx + pageSize;
            if (toIdx > routeList.size())
                toIdx = routeList.size();
            ret = routeList.subList(fromIdx, toIdx);
        }

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
     */
    public static Result getTrainRouteByCode(String trainCode) throws UnknownHostException, TravelPiException {
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
    public static Result searchTrainStationByLoc(String id) throws UnknownHostException, TravelPiException {
        MongoClient client = Utils.getMongoClient();
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("train_route");

//        QueryBuilder.start("locId").is(new ObjectId(id))

        return Results.TODO;
    }
}
