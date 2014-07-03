package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import controllers.TravelPiException;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 和路线规划相关的静态类
 *
 * @author Zephyre
 *         Created by zephyre on 7/2/14.
 */
public class Planner {

    /**
     * 为路线添加大交通信息
     *
     * @param detailNodes
     * @param fromLoc
     */
    public static void telomere(List<JsonNode> detailNodes, DBObject fromLoc, boolean start) throws TravelPiException {
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
