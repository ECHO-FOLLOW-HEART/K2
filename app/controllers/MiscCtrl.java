package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import core.Locality;
import core.POI;
import exception.ErrorCode;
import exception.TravelPiException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 其它
 *
 * @author Zephyre
 */
public class MiscCtrl extends Controller {
    public static Result postFeedback() throws UnknownHostException, TravelPiException {
        JsonNode feedback = request().body().asJson();
        ObjectId uid = null;
        try {
            uid = new ObjectId(feedback.get("uid").asText());
            DBCollection col = Utils.getMongoClient().getDB("user").getCollection("user_info");
            DBObject userItem = col.findOne(QueryBuilder.start("_id").is(uid).get());
            if (userItem == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", uid));
        } catch (NullPointerException ignored) {
        } catch (IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", feedback.get("uid").asText()));
        }
        String body = null;
        if (feedback.has("body"))
            body = feedback.get("body").asText();
        if (body == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "No body found.");

        String title = null;
        if (feedback.has("title"))
            title = feedback.get("title").asText();

        DBObject entry = new BasicDBObject();
        if (uid != null)
            entry.put("user", uid);
        entry.put("body", body);
        entry.put("time", new Date());

        JsonNode contact = feedback.get("contact");
        if (contact != null)
            entry.put("contact", JSON.parse(contact.toString()));

        MongoClient client = Utils.getMongoClient();
        DBCollection col = client.getDB("misc").getCollection("feedback");
        col.save(entry);

        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
    }

    /**
     * 获得更新信息
     *
     * @param platform    操作系统系统
     * @param platformVer 操作系统版本
     * @param appVer      App版本
     * @return
     */
    public static Result getUpdateInfo(String platform, String platformVer, String appVer) {
        ObjectNode ret = Json.newObject();
        ret.put("update", false);
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    /**
     * 根据搜索词获得提示
     *
     * @param word
     * @param pageSize
     * @return
     * @throws UnknownHostException
     */
    public static Result getSuggestions(String word, int loc, int vs, int pageSize) throws UnknownHostException, TravelPiException {
        ObjectNode ret = Json.newObject();
        if (loc != 0) {
            DBObject extra = BasicDBObjectBuilder.start("level", BasicDBObjectBuilder.start("$gt", 1).get()).get();
            List<JsonNode> locSug = getSpecSug(word, pageSize, "zhName", "geo", "locality", extra);
            ret.put("loc", Json.toJson(locSug));
        }
        if (vs != 0) {
            List<JsonNode> vsSug = getSpecSug(word, pageSize, "name", "poi", "view_spot", null);
            ret.put("vs", Json.toJson(vsSug));
        }
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    private static List<JsonNode> getSpecSug(String word, int pageSize, String nameField, String dbName, String colName, DBObject extra) throws UnknownHostException, TravelPiException {
        Pattern pattern = Pattern.compile("^" + word);
        DBCollection colLoc = Utils.getMongoClient().getDB(dbName).getCollection(colName);

        DBObject qb = QueryBuilder.start(nameField).regex(pattern).get();
        if (extra != null && extra.keySet().size() > 0)
            qb.putAll(extra);
        DBCursor cursor = colLoc.find(qb, BasicDBObjectBuilder.start(nameField, 1).add("ratings", 1).get())
                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get()).limit(pageSize);
        List<JsonNode> results = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            try {
                ObjectNode node = Json.newObject();
                String name = loc.get(nameField).toString();
                String id = loc.get("_id").toString();
                int score = 0;
                try {
                    score = (int) ((DBObject) (loc.get("ratings"))).get("score");
                } catch (NullPointerException ignored) {
                }
                if (score > 0)
                    node.put("score", score);

                node.put("name", name);
                node.put("_id", id);
                results.add(node);
            } catch (NullPointerException ignored) {
            }
        }
        return results;
    }


    /**
     * 发现目的地
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result exploreLoc(int showDetails, int page, int pageSize) throws UnknownHostException, TravelPiException {
        String uid = request().getQueryString("uid");
        boolean detailFlag = (showDetails > 0);

        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality");
        BasicDBObjectBuilder facet = BasicDBObjectBuilder.start("zhName", 1);
        if (detailFlag)
            facet = facet.add("imageList", 1).add("desc", 1).add("tags", 1).add("ratings.score", 1);
        DBCursor cursor = col.find(QueryBuilder.start("level").is(2).get(), facet.get())
                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get())
                .skip(page * pageSize).limit(pageSize);
        List<JsonNode> results = new ArrayList<>();

        while (cursor.hasNext()) {
            DBObject queryRet = cursor.next();
            ObjectNode node = Json.newObject();
            node.put("_id", queryRet.get("_id").toString());
            node.put("name", queryRet.get("zhName").toString());

            // 显示详细信息
            if (detailFlag) {
                Object imageList = queryRet.get("imageList");
                if (imageList != null)
                    node.put("imageList", Json.toJson(imageList));
                Object desc = queryRet.get("desc");
                if (desc != null)
                    node.put("desc", desc.toString());
                Object tags = queryRet.get("tags");
                if (tags != null)
                    node.put("tags", Json.toJson(tags));
            }
            results.add(node);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }

    /**
     * 发现POI
     *
     * @param showDetails
     * @param vs
     * @param restaurant
     * @param hotel
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePOI(int showDetails, int vs, int restaurant, int hotel, int page, int pageSize) throws UnknownHostException, TravelPiException {
        boolean detailFlag = (showDetails > 0);
        String uid = request().getQueryString("uid");

        ObjectNode results = Json.newObject();
        if (vs != 0) {
            List<JsonNode> vsList = explorePOIHlp(detailFlag, "view_spot", uid, page, pageSize);
            results.put("viewSpot", Json.toJson(vsList));
        }
        if (restaurant != 0) {
            List<JsonNode> vsList = explorePOIHlp(detailFlag, "restaurant", uid, page, pageSize);
            results.put("restaurant", Json.toJson(vsList));
        }
        if (hotel != 0) {
            List<JsonNode> vsList = explorePOIHlp(detailFlag, "hotel", uid, page, pageSize);
            results.put("hotel", Json.toJson(vsList));
        }

        return Utils.createResponse(ErrorCode.NORMAL, results);
    }


    /**
     * 发现景点
     *
     * @param detailFlag
     * @param uid
     * @param page
     * @param pageSize
     * @return
     */
    private static List<JsonNode> explorePOIHlp(boolean detailFlag, String colName, String uid, int page, int pageSize) throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
        BasicDBObjectBuilder facet = BasicDBObjectBuilder.start("name", 1);
        if (detailFlag)
            facet = facet.add("imageList", 1).add("intro.desc", 1).add("tags", 1).add("ratings.score", 1);
//        DBObject explain = col.find(new BasicDBObject(), facet.get())
//                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get())
//                .skip(page * pageSize).limit(pageSize).explain();

        DBCursor cursor = col.find(new BasicDBObject(), facet.get())
                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get())
                .skip(page * pageSize).limit(pageSize);

        List<JsonNode> results = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject queryRet = cursor.next();
            ObjectNode node = Json.newObject();
            node.put("_id", queryRet.get("_id").toString());
            node.put("name", queryRet.get("name").toString());

            // 显示详细信息
            if (detailFlag) {
                Object imageList = queryRet.get("imageList");
                if (imageList != null)
                    node.put("imageList", Json.toJson(imageList));
                try {
                    Object desc = ((DBObject) queryRet.get("intro")).get("desc");
                    if (desc != null)
                        node.put("desc", desc.toString());
                } catch (NullPointerException ignored) {
                }
                Object tags = queryRet.get("tags");
                if (tags != null)
                    node.put("tags", Json.toJson(tags));
            }
            results.add(node);
        }
        return results;
    }


    /**
     * 广义的发现接口（通过一系列开关来控制）
     *
     * @param loc
     * @param vs
     * @param hotel
     * @param restaurant
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explore(int details, int loc, int vs, int hotel, int restaurant, int page, int pageSize) throws UnknownHostException, TravelPiException {
        boolean detailsFlag = (details != 0);
        DBObject results = new BasicDBObject();

        // 发现城市
        if (loc != 0) {
            BasicDBList retLocList = new BasicDBList();

            for (Object obj : Locality.explore(detailsFlag, page, pageSize)) {
                retLocList.add(Locality.getLocDetailsJson((DBObject) obj, 2));
            }
            results.put("loc", retLocList);
        }

        List<POI.POIType> poiKeyList = new ArrayList<>();
        if (vs != 0)
            poiKeyList.add(POI.POIType.VIEW_SPOT);
        if (hotel != 0)
            poiKeyList.add(POI.POIType.HOTEL);
        if (restaurant != 0)
            poiKeyList.add(POI.POIType.RESTAURANT);

        HashMap<POI.POIType, String> poiMap = new HashMap<POI.POIType, String>() {
            {
                put(POI.POIType.VIEW_SPOT, "vs");
                put(POI.POIType.HOTEL, "hotel");
                put(POI.POIType.RESTAURANT, "restaurant");
            }
        };

        for (POI.POIType poiType : poiKeyList) {
            // 发现POI
            BasicDBList retPoiList = new BasicDBList();
            for (Object obj : POI.explore(detailsFlag, poiType, null, page, pageSize)) {
                DBObject poiObj = (DBObject) obj;
                BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("_id", poiObj.get("_id").toString()).add("name", poiObj.get("name"));
                if (detailsFlag) {
                    for (String k : new String[]{"imageList", "tags"}) {
                        BasicDBList retValList = new BasicDBList();
                        Object valList = poiObj.get(k);
                        if (valList == null || !(valList instanceof BasicDBList))
                            valList = new BasicDBList();
                        for (Object val : (BasicDBList) valList)
                            retValList.add(val.toString());
                        builder.add(k, retValList);
                    }
                    Object tmp = poiObj.get("desc");
                    builder.add("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), Constants.ABBREVIATE_LEN)));
                    builder.add("favorCnt", 0).add("voteCnt", 0);
                }

                Object tmp = poiObj.get("geo");
                if (tmp == null || !(tmp instanceof DBObject))
                    tmp = new BasicDBObject();
                DBObject geoNode = (DBObject) tmp;
                BasicDBObjectBuilder geoBuilder = BasicDBObjectBuilder.start();
                geoBuilder.add("locId", geoNode.get("locId").toString()).add("locName", geoNode.get("locName"));
                builder.add("geo", geoBuilder.get());

                retPoiList.add(builder.get());
            }
            results.put(poiMap.get(poiType), retPoiList);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }
}
