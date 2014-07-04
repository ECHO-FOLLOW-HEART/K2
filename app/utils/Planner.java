package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import org.bson.types.ObjectId;
import play.filters.csrf.CSRF;
import play.libs.Json;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 和路线规划相关的静态类
 *
 * @author Zephyre
 *         Created by zephyre on 7/2/14.
 */
public class Planner {

    public static DBObject generateUgcPlan(String planId, String fromLocId, String backLocId, Calendar baseDate)
            throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
        DBCollection locCol = Utils.getMongoClient().getDB("geo").getCollection("locality");
        DBCollection hotelCol = Utils.getMongoClient().getDB("poi").getCollection("hotel");

        DBObject plan, fromLoc, backLoc;
        try {
            plan = col.findOne(QueryBuilder.start("_id").is(new ObjectId(planId)).get());
            if (plan == null)
                throw new NullPointerException();
            fromLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(fromLocId)).get());
            if (fromLoc == null)
                throw new NullPointerException();
            if (backLocId == null || backLocId.isEmpty())
                backLocId = fromLocId;
            backLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(backLocId)).get());
            if (backLoc == null)
                throw new NullPointerException();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId), e);
        }

        BasicDBList details = (BasicDBList) plan.get("details");

        if (details == null || details.size() == 0)
            return plan;

        // 如果出发地和目的地不在一个城市，并且没有大交通信息，则添加出发端粒
        // TODO 判断出发地和目的地是否为一个城市
        BasicDBList activities = (BasicDBList) ((DBObject) details.get(0)).get("actv");
        DBObject actv = (DBObject) activities.get(0);
        if (!actv.get("type").equals("majorTraffic"))
            telomere(details, fromLoc, baseDate, true);

        // 进行日期标注
        Calendar curDate = Calendar.getInstance(Utils.getDefaultTimeZone());
        curDate.setTimeInMillis(baseDate.getTimeInMillis());
        for (Object obj : details) {
            DBObject dayNode = (DBObject) obj;
            if (!dayNode.containsField("date"))
                dayNode.put("date", curDate);

            Calendar newCurDate = Calendar.getInstance(Utils.getDefaultTimeZone());
            newCurDate.setTimeInMillis(curDate.getTimeInMillis());
            curDate = newCurDate;
            curDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        activities = (BasicDBList) ((DBObject) details.get(details.size() - 1)).get("actv");
        actv = (DBObject) activities.get(activities.size() - 1);
        if (!actv.get("type").equals("majorTraffic")) {
            Calendar lastCal = Calendar.getInstance(Utils.getDefaultTimeZone());
            lastCal.setTimeInMillis(((Calendar) ((DBObject) details.get(details.size() - 1)).get("date")).getTimeInMillis());
            lastCal.add(Calendar.DAY_OF_YEAR, 1);
            telomere(details, fromLoc, lastCal, false);
        }

        // 处理住宿
        for (int i = 0; i < details.size() - 1; i++) {
            DBObject dayNode = (DBObject) details.get(i);
            activities = (BasicDBList) dayNode.get("actv");
            actv = (DBObject) activities.get(activities.size() - 1);
            if (actv.get("type").equals("hotel"))
                continue;

            ObjectId locId;
            String locName;
            if (actv.get("type").equals("majorTraffic")) {
                locId = (ObjectId) actv.get("arrLocId");
                locName = actv.get("arrLocName").toString();
            } else {

                locId = (ObjectId) actv.get("locId");
                locName = actv.get("locName").toString();
            }

            DBCursor cursor = hotelCol.find(QueryBuilder.start("geo.locality._id").is(locId).get())
                    .sort(BasicDBObjectBuilder.start("ratings.score", -1).get())
                    .limit(1);
            if (!cursor.hasNext())
                continue;
            DBObject hotel = cursor.next();

            DBObject hotelActv = BasicDBObjectBuilder.start().add("itemId", hotel.get("_id"))
                    .add("itemName", hotel.get("name"))
                    .add("locId", locId)
                    .add("locName", locName)
                    .add("type", "hotel").get();
            activities.add(hotelActv);
        }

        return plan;
    }


    /**
     * 为路线添加大交通信息
     *
     * @param detailNodes
     * @param fromLoc
     */
    private static void telomere(BasicDBList detailNodes, DBObject fromLoc, Calendar baseDate, boolean start) throws TravelPiException {
        if (detailNodes == null || detailNodes.size() == 0)
            return;

        String depLocId, arrLocId;
        List<Calendar> epTimeLimits;
        if (start) {
            // 出发时间限制
            Calendar depCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            depCal.setTimeInMillis(baseDate.getTimeInMillis());
            depCal.set(Calendar.HOUR, 6);
            depCal.set(Calendar.MINUTE, 0);
            depCal.set(Calendar.SECOND, 0);
            depCal.set(Calendar.MILLISECOND, 0);

            Calendar arrCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            arrCal.setTimeInMillis(depCal.getTimeInMillis());
            arrCal.add(Calendar.DAY_OF_YEAR, 1);
            arrCal.set(Calendar.HOUR, 11);

            epTimeLimits = new ArrayList<>();
            epTimeLimits.add(depCal);
            epTimeLimits.add(arrCal);
            depLocId = fromLoc.get("_id").toString();

            BasicDBList activities = (BasicDBList) ((DBObject) detailNodes.get(0)).get("actv");
            arrLocId = ((DBObject) activities.get(0)).get("locId").toString();
        } else {
            // 返回时间限制
            Calendar depCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            depCal.setTimeInMillis(baseDate.getTimeInMillis());
            depCal.set(Calendar.HOUR, 14);
            depCal.set(Calendar.MINUTE, 0);
            depCal.set(Calendar.SECOND, 0);
            depCal.set(Calendar.MILLISECOND, 0);

            Calendar arrCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            arrCal.setTimeInMillis(depCal.getTimeInMillis());
            arrCal.add(Calendar.DAY_OF_YEAR, 1);
            arrCal.set(Calendar.HOUR, 23);

            epTimeLimits = new ArrayList<>();
            epTimeLimits.add(depCal);
            epTimeLimits.add(arrCal);

            BasicDBList activities = (BasicDBList) ((DBObject) detailNodes.get(detailNodes.size() - 1)).get("actv");
            depLocId = ((DBObject) activities.get(activities.size() - 1)).get("locId").toString();
            arrLocId = fromLoc.get("_id").toString();
        }

        // 查询列车信息
        BasicDBList routeList = Traffic.searchTrainRoute(depLocId, arrLocId, null,
                null, null, epTimeLimits, null, Traffic.SortField.TIME_COST, Traffic.SortType.ASC);
        if (routeList.size() > 0) {
            DBObject route = (DBObject) routeList.get(0);

            // 是否需要单独分配一天
            // TODO 目前一律需要
            final DBObject actv = BasicDBObjectBuilder.start().add("itemId", route.get("_id"))
                    .add("depStopId", ((DBObject) route.get("dep")).get("stopId"))
                    .add("depStopName", ((DBObject) route.get("dep")).get("stopName"))
                    .add("depLocId", ((DBObject) route.get("dep")).get("locId"))
                    .add("depLocName", ((DBObject) route.get("dep")).get("locName"))
                    .add("arrStopId", ((DBObject) route.get("arr")).get("stopId"))
                    .add("arrStopName", ((DBObject) route.get("arr")).get("stopName"))
                    .add("arrLocId", ((DBObject) route.get("arr")).get("locId"))
                    .add("arrLocName", ((DBObject) route.get("arr")).get("locName"))
                    .add("type", "majorTraffic")
                    .add("subType", "train").get();
            Date depTime = (Date) route.get("depTime");
            Date arrTime = (Date) route.get("arrTime");
            long duration = arrTime.getTime() - depTime.getTime();
            Calendar depCal = Calendar.getInstance(Utils.getDefaultTimeZone());
            depCal.setTime(depTime);
            depCal.set(Calendar.YEAR, baseDate.get(Calendar.YEAR));
            depCal.set(Calendar.DAY_OF_YEAR, baseDate.get(Calendar.DAY_OF_YEAR));
            Calendar arrCal = Calendar.getInstance(Utils.getDefaultTimeZone());
            arrCal.setTimeInMillis(depCal.getTimeInMillis() + duration);

            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            fmt.setTimeZone(Utils.getDefaultTimeZone());

            actv.put("depTime", fmt.format(depCal.getTime()));
            actv.put("arrTime", fmt.format(arrCal.getTime()));

            DBObject dayNode = BasicDBObjectBuilder.start()
                    .add("date", baseDate)
                    .add("actv", new BasicDBList() {
                        {
                            add(actv);
                        }
                    }).get();
            if (start)
                detailNodes.add(0, dayNode);
            else
                detailNodes.add(dayNode);
        }
    }

    /**
     * 为路线添加大交通信息
     *
     * @param detailNodes
     * @param fromLoc
     */
    public static void telomereOld(List<JsonNode> detailNodes, DBObject fromLoc, boolean start) throws TravelPiException {
        if (detailNodes == null || detailNodes.size() == 0)
            return;

        String depLocId, arrLocId;
        List<Calendar> epTimeLimits, arrTimeLimits;
        if (start) {
            // 出发时间限制
            Calendar depCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            depCal.set(1980, Calendar.JANUARY, 1, 17, 30);
            Calendar arrCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            arrCal.set(1980, Calendar.JANUARY, 2, 11, 0);
            epTimeLimits = new ArrayList<>();
            epTimeLimits.add(depCal);
            epTimeLimits.add(arrCal);
            depLocId = fromLoc.get("_id").toString();
            arrLocId = detailNodes.get(0).get(0).get("loc").get("_id").asText();
        } else {
            // 返回时间限制
            Calendar depCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            depCal.set(1980, Calendar.JANUARY, 1, 12, 0);
            Calendar arrCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            arrCal.set(1980, Calendar.JANUARY, 2, 11, 30);
            epTimeLimits = new ArrayList<>();
            epTimeLimits.add(depCal);
            epTimeLimits.add(arrCal);
            depLocId = detailNodes.get(detailNodes.size() - 1).get(0).get("loc").get("_id").asText();
            arrLocId = fromLoc.get("_id").toString();
        }

        // 查询列车信息
        BasicDBList routeList = Traffic.searchTrainRoute(depLocId, arrLocId, null,
                null, null, epTimeLimits, null, Traffic.SortField.TIME_COST, Traffic.SortType.ASC);
        if (routeList.size() > 0) {
            DBObject route = (DBObject) routeList.get(0);

            final ObjectNode telomereA = Json.newObject();
            ObjectNode station = Json.newObject();
            station.put("_id", ((DBObject) route.get("dep")).get("stopId").toString());
            station.put("name", ((DBObject) route.get("dep")).get("stopName").toString());
            station.put("type", "train");
            station.put("code", route.get("code").toString());
            DateFormat fmt = new SimpleDateFormat("HH:mm");
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            fmt.setTimeZone(tz);
            station.put("ts", fmt.format(route.get("depTime")));
            telomereA.put("item", station);
            ObjectNode loc = Json.newObject();
            loc.put("_id", ((DBObject) route.get("dep")).get("locId").toString());
            station.put("name", ((DBObject) route.get("dep")).get("locName").toString());
            telomereA.put("loc", loc);

            final ObjectNode telomereB = Json.newObject();
            station = Json.newObject();
            station.put("_id", ((DBObject) route.get("arr")).get("stopId").toString());
            station.put("name", ((DBObject) route.get("arr")).get("stopName").toString());
            station.put("type", "train");
            station.put("code", route.get("code").toString());
            station.put("ts", fmt.format(route.get("arrTime")));
            telomereB.put("item", station);
            loc = Json.newObject();
            loc.put("_id", ((DBObject) route.get("arr")).get("locId").toString());
            station.put("name", ((DBObject) route.get("arr")).get("locName").toString());
            telomereB.put("loc", loc);

            List<JsonNode> node = new ArrayList<JsonNode>() {
                {
                    add(telomereA);
                    add(telomereB);
                }
            };
            if (start)
                detailNodes.add(0, Json.toJson(node));
            else
                detailNodes.add(Json.toJson(node));
        }
    }
}
