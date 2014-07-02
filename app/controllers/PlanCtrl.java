package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import org.apache.commons.lang3.*;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
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
     * 查询线路的详细信息。
     *
     * @param planId
     * @return
     * @throws UnknownHostException
     */
    public static Result templatePlanDetails(String planId) throws UnknownHostException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");

        DBObject plan;
        try {
            plan = col.findOne(QueryBuilder.start("_id").is(new ObjectId(planId)).get());
            if (plan == null)
                throw new NullPointerException();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
        }

        ObjectNode ret = Json.newObject();
        ret.put("_id", plan.get("_id").toString());
        DBObject loc = (DBObject) plan.get("loc");
        ret.put("loc", Json.toJson(BasicDBObjectBuilder.start("_id", loc.get("_id").toString())
                .add("name", loc.get("name").toString()).get()));

        for (String key : new String[]{"target", "title", "tags", "days", "intro", "imageList", "viewCnt"}) {
            Object tmp = plan.get(key);
            if (tmp != null)
                ret.put(key, Json.toJson(tmp));
        }

        // 获取路线详情
        BasicDBList detailsList = (BasicDBList) plan.get("details");

        int curDay = -1;
        // 整个路线详情列表
        List<JsonNode> detailNodes = new ArrayList<>();
        // 单日路线详情列表（只处理景区）
        List<JsonNode> detailNodesD = new ArrayList<>();

        if (detailsList != null) {

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
}
