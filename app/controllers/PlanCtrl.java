package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.PlanAPI;
import core.PlanAPIOld;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.plan.Plan;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Planner;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


/**
 * 路线相关
 *
 * @author Zephyre
 */
public class PlanCtrl extends Controller {


    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws UnknownHostException
     */
    public static Result getPlanFromTemplates(String planId, String fromLocId, String backLocId, int traffic, int hotel) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 3);
            Plan plan = PlanAPI.doPlanner(planId, fromLocId, backLocId, cal);
            return Utils.createResponse(ErrorCode.NORMAL, plan.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws UnknownHostException
     */
    public static Result getPlanFromTemplatesOld(String planId, String fromLocId, String backLocId, int traffic, int hotel) {
        try {
            DBObject plan = PlanAPIOld.getPlanOld(planId);

            DBCollection locCol = Utils.getMongoClient().getDB("geo").getCollection("locality");
            DBObject fromLoc, backLoc;
            try {
                fromLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(fromLocId)).get());
                if (fromLoc == null)
                    throw new NullPointerException();
                if (backLocId == null || backLocId.isEmpty())
                    backLocId = fromLocId;
                backLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(backLocId)).get());
                if (backLoc == null)
                    throw new NullPointerException();
                backLocId = backLoc.get("_id").toString();
            } catch (IllegalArgumentException | NullPointerException e) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
            }

            // 获得基准时间（默认为3天后）
            Calendar calBase = Calendar.getInstance();
            calBase.add(Calendar.DAY_OF_YEAR, 3);
            calBase.set(Calendar.HOUR, 0);
            calBase.set(Calendar.MINUTE, 0);
            calBase.set(Calendar.SECOND, 0);
            calBase.set(Calendar.MILLISECOND, 0);

            DBObject ret1 = Planner.generateUgcPlan(plan, fromLocId, backLocId, calBase);

            return Utils.createResponse(ErrorCode.NORMAL, Utils.bsonToJson(ret1));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

//        ObjectNode ret = Json.newObject();
//        ret.put("_id", plan.get("_id").toString());
//        DBObject loc = (DBObject) plan.get("loc");
//        ret.put("loc", Json.toJson(BasicDBObjectBuilder.start("_id", loc.get("_id").toString())
//                .add("name", loc.get("name").toString()).get()));
//
//        for (String key : new String[]{"target", "title", "tags", "days", "desc", "imageList", "viewCnt"}) {
//            Object tmp = plan.get(key);
//            if (tmp != null)
//                ret.put(key, Json.toJson(tmp));
//        }
//
//        // 获取路线详情
//        BasicDBList detailsList = (BasicDBList) plan.get("details");
//
//
//        // 整个路线详情列表
//        List<JsonNode> detailNodes = new ArrayList<>();
//
//        if (detailsList != null) {
//            int curDay = -1;
//            // 单日路线详情列表（只处理景区）
//            List<JsonNode> detailNodesD = new ArrayList<>();
//
//            // 按照dayIdx和idx的顺序进行排序
//            Collections.sort(detailsList, new Comparator<Object>() {
//                @Override
//                public int compare(Object o1, Object o2) {
//                    int dayIdx1 = (int) ((DBObject) o1).get("dayIdx");
//                    int dayIdx2 = (int) ((DBObject) o2).get("dayIdx");
//                    int idx1 = (int) ((DBObject) o1).get("idx");
//                    int idx2 = (int) ((DBObject) o2).get("idx");
//
//                    int c = dayIdx1 - dayIdx2;
//                    if (c == 0)
//                        c = idx1 - idx2;
//                    return c;
//                }
//            });
//
//            DBCollection vsCol = Utils.getMongoClient().getDB("poi").getCollection("view_spot");
//
//            for (Object aDetailsList : detailsList) {
//                DBObject detailsItem = (DBObject) aDetailsList;
//                DBObject item = (DBObject) detailsItem.get("item");
//                if (!item.get("type").equals("vs"))
//                    continue;
//
//                ObjectNode node = Json.newObject();
//
//                int dayIdx = (int) detailsItem.get("dayIdx");
//                node.put("dayIdx", dayIdx);
//
//                // 景点
//                ObjectNode vsNode = Json.newObject();
//                vsNode.put("_id", item.get("_id").toString());
//                vsNode.put("name", item.get("name").toString());
//                vsNode.put("type", "vs");
//                DBObject vs = vsCol.findOne(QueryBuilder.start("_id").is(item.get("_id")).get());
//                Object tmp = vs.get("tags");
//                if (tmp != null)
//                    vsNode.put("tags", Json.toJson(tmp));
//                tmp = vs.get("intro");
//                if (tmp != null) {
//                    tmp = ((DBObject) tmp).get("desc");
//                    if (tmp != null)
//                        vsNode.put("desc", StringUtils.abbreviate(tmp.toString(), 64));
//                }
//                node.put("item", vsNode);
//
//                DBObject stopLoc = (DBObject) detailsItem.get("loc");
//                ObjectNode locNode = Json.newObject();
//                locNode.put("_id", stopLoc.get("_id").toString());
//                locNode.put("name", stopLoc.get("name").toString());
//                node.put("loc", locNode);
//
//                if (dayIdx != curDay) {
//                    if (!detailNodesD.isEmpty())
//                        detailNodes.add(Json.toJson(detailNodesD));
//                    detailNodesD = new ArrayList<>();
//                }
//                detailNodesD.add(node);
//                curDay = dayIdx;
//            }
//            if (!detailNodesD.isEmpty())
//                detailNodes.add(Json.toJson(detailNodesD));
//        }
//
////        if (traffic != 0) {
////            // TODO 交通的起止时间，需要根据当天的游玩景点而定。
////            // 添加大交通
////            Planner.telomere(detailsList, fromLoc, calBase, true);
////            Planner.telomere(detailsList, backLoc, calBase, false);
////        }
//
//        // 添加每晚住宿
//
//        ret.put("details", Json.toJson(detailNodes));
//
//        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    /**
     * 保存用户的路线
     *
     * @param userId
     * @return
     */
    public static Result saveUGCPlan(String userId) throws UnknownHostException, TravelPiException {
        ObjectNode plan = (ObjectNode) request().body().asJson();
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");

        BasicDBObject ugcPlan = new BasicDBObject((java.util.Map) plan);

        String planId;
        ObjectId planOid, userOid;
        try {
            userOid = new ObjectId(userId);
            DBCollection userCol = Utils.getMongoClient().getDB("user").getCollection("user_info");
            DBObject user = userCol.findOne(QueryBuilder.start("_id").is(userOid).get(), BasicDBObjectBuilder.start().get());
            if (user == null)
                throw new NullPointerException();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user ID: %s.", userId));
        }

        if (plan.has("_id")) {
            planId = plan.get("_id").asText();
            try {
                planOid = new ObjectId(planId);
                DBCollection planCol = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
                DBObject planItem = planCol.findOne(QueryBuilder.start("_id").is(planOid).get(), BasicDBObjectBuilder.start().get());
                if (planItem == null)
                    throw new NullPointerException();
            } catch (IllegalArgumentException | NullPointerException e) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s", planId));
            }
        } else
            planOid = ObjectId.get();


        return play.mvc.Results.TODO;
    }


    /**
     * 路线发现机制
     *
     * @param locId
     * @param sortField
     * @param sort
     * @param tag
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlans(String locId, String poiId, String sortField, String sort, String tag, int page, int pageSize) throws UnknownHostException, TravelPiException {
        List<JsonNode> results = new ArrayList<>();
        for (Iterator<Plan> it = PlanAPI.explore(locId, poiId, sort, tag, page, pageSize, sortField);
             it.hasNext(); )
            results.add(it.next().toJson());

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    /**
     * 路线发现机制
     *
     * @param locId
     * @param sortField
     * @param sort
     * @param tags
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlansOld(String locId, String poiId, String sortField, String sort, String tags, int page, int pageSize) throws UnknownHostException, TravelPiException {
        List<JsonNode> results = new ArrayList<>();
        for (Object tmp : PlanAPIOld.exploreOld(locId, poiId, sort, tags, page, pageSize, sortField)) {
            DBObject planNode = (DBObject) tmp;
            results.add(PlanAPIOld.getPlanJsonOld(planNode));
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }
}
