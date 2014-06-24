package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * POI相关信息。
 *
 * @author Zephyre
 */
public class POICtrl extends Controller {
    /**
     * 获得经典的详细信息。
     *
     * @param spotId 景点ID。
     * @throws UnknownHostException
     */
    public static Result getViewSpot(String spotId) throws UnknownHostException {
        MongoClient client = Utils.getMongoClient("localhost", 27017);
        DB db = client.getDB("view_spot");
        DBCollection col = db.getCollection("core");

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(spotId));
        DBObject result = col.findOne(query);

        if (result == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid view spot ID: %s.", spotId));

        ObjectNode json = (ObjectNode) Json.parse(result.toString());
        json.put("spotId", json.get("_id").get("$oid"));
        json.remove("_id");
        return Utils.createResponse(ErrorCode.NORMAL, json);

    }

    /**
     * 搜索景点的ID。
     *
     * @param locality   景点所在地区的ID。
     * @param tagFilter  按照标签对景点进行过滤。
     * @param sortFilter 景点列表排序的字段，目前仅支持rating，即按照评分进行排序。
     * @param sort       升序(asc)还是降序(desc)
     * @param page       起始页码。
     * @param pageSize   页面大小。
     */
    public static Result searchViewSpot(long locality, String tagFilter, String sortFilter, String sort,
                                        int page, int pageSize) throws UnknownHostException {
        MongoClient client = Utils.getMongoClient("localhost", 27017);
        DB db = client.getDB("view_spot");
        DBCollection col = db.getCollection("core");

        BasicDBObject query = new BasicDBObject();
        query.put("geo.locality.localityId", locality);
        if (tagFilter != null && !tagFilter.isEmpty())
            query.put("tags", tagFilter);
        DBCursor cursor = col.find(query).skip(page * pageSize).limit(pageSize);
        if (sort.equals("asc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", 1));
        else if (sort.equals("desc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", -1));

        List<JsonNode> resultList = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject item = cursor.next();
            ObjectNode node = Json.newObject();
            node.put("spotId", item.get("_id").toString());

            Object tmp = item.get("ratings");
            if (tmp != null)
                node.put("ratings", Json.parse(tmp.toString()));
            tmp = item.get("tags");
            if (tmp != null)
                node.put("tags", Json.parse(tmp.toString()));

            tmp = item.get("name");
            if (tmp != null)
                node.put("name", tmp.toString());

            tmp = ((DBObject) item.get("introduction")).get("description");
            if (tmp != null) {
                String desc = tmp.toString();
                // 只取前36个字符。
                if (desc.length() > 36)
                    desc = desc.substring(0, 36);
                node.put("description", desc);
            }

            tmp = item.get("geo");
            if (tmp != null)
                node.put("geo", Json.parse(tmp.toString()));

            tmp = item.get("imageList");
            if (tmp != null)
                node.put("imageList", Json.parse(tmp.toString()));

            resultList.add(node);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(resultList));
    }
}
