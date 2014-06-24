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

/**
 * POI相关信息。
 *
 * @author Zephyre
 */
public class POICtrl extends Controller {
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
}
