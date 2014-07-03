package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import org.apache.commons.lang3.*;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Planner;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public static Result getPlanFromTemplates(String planId, String fromLocId, String backLocId, int traffic, int hotel) throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
        DBCollection locCol = Utils.getMongoClient().getDB("geo").getCollection("locality");
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
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
        }

        // 获得基准时间

        ObjectNode ret = Json.newObject();
        ret.put("_id", plan.get("_id").toString());
        DBObject loc = (DBObject) plan.get("loc");
        ret.put("loc", Json.toJson(BasicDBObjectBuilder.start("_id", loc.get("_id").toString())
                .add("name", loc.get("name").toString()).get()));

        for (String key : new String[]{"target", "title", "tags", "days", "desc", "imageList", "viewCnt"}) {
            Object tmp = plan.get(key);
            if (tmp != null)
                ret.put(key, Json.toJson(tmp));
        }

        // 获取路线详情
        BasicDBList detailsList = (BasicDBList) plan.get("details");
        // 整个路线详情列表
        List<JsonNode> detailNodes = new ArrayList<>();

        if (detailsList != null) {
            int curDay = -1;
            // 单日路线详情列表（只处理景区）
            List<JsonNode> detailNodesD = new ArrayList<>();

            // 按照dayIdx和idx的顺序进行排序
            Collections.sort(detailsList, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    int dayIdx1 = (int) ((DBObject) o1).get("dayIdx");
                    int dayIdx2 = (int) ((DBObject) o2).get("dayIdx");
                    int idx1 = (int) ((DBObject) o1).get("idx");
                    int idx2 = (int) ((DBObject) o2).get("idx");

                    int c = dayIdx1 - dayIdx2;
                    if (c == 0)
                        c = idx1 - idx2;
                    return c;
                }
            });

            DBCollection vsCol = Utils.getMongoClient().getDB("poi").getCollection("view_spot");

            for (Object aDetailsList : detailsList) {
                DBObject detailsItem = (DBObject) aDetailsList;
                DBObject item = (DBObject) detailsItem.get("item");
                if (!item.get("type").equals("vs"))
                    continue;

                ObjectNode node = Json.newObject();

                int dayIdx = (int) detailsItem.get("dayIdx");
                node.put("dayIdx", dayIdx);

                // 景点
                ObjectNode vsNode = Json.newObject();
                vsNode.put("_id", item.get("_id").toString());
                vsNode.put("name", item.get("name").toString());
                vsNode.put("type", "vs");
                DBObject vs = vsCol.findOne(QueryBuilder.start("_id").is(item.get("_id")).get());
                Object tmp = vs.get("tags");
                if (tmp != null)
                    vsNode.put("tags", Json.toJson(tmp));
                tmp = vs.get("intro");
                if (tmp != null) {
                    tmp = ((DBObject) tmp).get("desc");
                    if (tmp != null)
                        vsNode.put("desc", StringUtils.abbreviate(tmp.toString(), 64));
                }
                node.put("item", vsNode);

                DBObject stopLoc = (DBObject) detailsItem.get("loc");
                ObjectNode locNode = Json.newObject();
                locNode.put("_id", stopLoc.get("_id").toString());
                locNode.put("name", stopLoc.get("name").toString());
                node.put("loc", locNode);

                if (dayIdx != curDay) {
                    if (!detailNodesD.isEmpty())
                        detailNodes.add(Json.toJson(detailNodesD));
                    detailNodesD = new ArrayList<>();
                }
                detailNodesD.add(node);
                curDay = dayIdx;
            }
            if (!detailNodesD.isEmpty())
                detailNodes.add(Json.toJson(detailNodesD));
        }

        if (traffic != 0) {
            // TODO 交通的起止时间，需要根据当天的游玩景点而定。
            // 添加大交通
            Planner.telomere(detailNodes, fromLoc, true);
            Planner.telomere(detailNodes, backLoc, false);
        }

        // 添加每晚住宿

        ret.put("details", Json.toJson(detailNodes));

        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    /**
     * 保存用户的路线
     *
     * @param userId
     * @return
     */
    public static Result saveUGCPlan(String userId) throws UnknownHostException {
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
     * @param tags
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlans(String locId, String sortField, String sort, String tags, int page, int pageSize) throws UnknownHostException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
        DBCursor cursor;

        try {
            QueryBuilder builder = QueryBuilder.start("loc._id").is(new ObjectId(locId));
            if (tags != null && !tags.isEmpty())
                builder = builder.and("tags").is(tags);
            int sortVal = 1;
            if (sort != null && (sort.equals("asc") || sort.equals("desc")))
                sortVal = sort.equals("asc") ? 1 : -1;
            cursor = col.find(builder.get());
            if (sortField != null && !sortField.isEmpty()) {
                switch (sortField) {
                    case "days":
                        cursor.sort(BasicDBObjectBuilder.start("days", sortVal).get());
                        break;
                    case "hot":
                        cursor.sort(BasicDBObjectBuilder.start("viewCnt", sortVal).get());
                }
            }
            cursor.skip(page * pageSize).limit(pageSize);
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
        }

        List<JsonNode> results = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject item = cursor.next();

            ObjectNode node = Json.newObject();
            node.put("_id", item.get("_id").toString());
            DBObject loc = (DBObject) item.get("loc");
            node.put("loc", Json.toJson(BasicDBObjectBuilder.start("_id", loc.get("_id").toString())
                    .add("name", loc.get("name").toString()).get()));
            node.put("title", item.get("title").toString());
            node.put("days", (int) item.get("days"));
            Object intro = item.get("intro");
            if (intro != null)
                node.put("intro", intro.toString());
            Object itemTags = item.get("tags");
            if (itemTags != null)
                node.put("tags", Json.toJson(itemTags));
            Object viewCnt = item.get("viewCnt");
            if (viewCnt != null)
                node.put("viewCnt", (int) viewCnt);

            node.put("imageList", Json.toJson(item.get("imageList")));
            results.add(node);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }
}
