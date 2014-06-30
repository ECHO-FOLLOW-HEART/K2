package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 其它
 *
 * @author Zephyre
 */
public class MiscCtrl extends Controller {
    public static Result postFeedback() throws UnknownHostException {
        JsonNode feedback = request().body().asJson();
        ObjectId uid = null;
        try {
            uid = new ObjectId(feedback.get("uid").asText());
            DBCollection col = Utils.getMongoClient().getDB("user").getCollection("user_info");
            DBObject userItem = col.findOne(QueryBuilder.start("_id").is(uid).get());
            if (userItem == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", uid));
        } catch (NullPointerException ignored) {
        } catch (IllegalArgumentException e) {
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
    public static Result getSuggestions(String word, int pageSize) throws UnknownHostException {
        DBObject extra = BasicDBObjectBuilder.start("level", BasicDBObjectBuilder.start("$lt", 3).get()).get();
        List<JsonNode> locSug = getSpecSug(word, pageSize, "zhName", "geo", "locality", extra);
        List<JsonNode> vsSug = getSpecSug(word, pageSize, "name", "poi", "view_spot", null);

        ObjectNode ret = Json.newObject();
        ret.put("loc", Json.toJson(locSug));
        ret.put("vs", Json.toJson(vsSug));
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    private static List<JsonNode> getSpecSug(String word, int pageSize, String nameField, String dbName, String colName, DBObject extra) throws UnknownHostException {
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
}
