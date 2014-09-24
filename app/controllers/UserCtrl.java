package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.*;
import core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 用户相关的Controller。
 *
 * @author Zephyre
 */
public class UserCtrl extends Controller {

    /**
     * 通过id获得用户详细信息。
     *
     * @param userId
     * @return
     */
    public static Result getUserProfileById(String userId) throws UnknownHostException {
        try {
            ObjectId uid = new ObjectId(userId);
            DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("user_info");
            DBObject userItem = col.findOne(QueryBuilder.start("_id").is(uid).get());
            if (userItem == null)
                throw new IllegalArgumentException();

            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("_id", uid.toString());
            if (userItem.get("name") != null)
                builder.add("name", userItem.get("name"));
            if (userItem.get("udid") != null)
                builder.add("udid", userItem.get("udid"));
            if (userItem.get("regTime") != null) {
                final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
                fmt.setTimeZone(tz);
                builder.add("regTime", fmt.format(userItem.get("regTime")));
            }

            return Utils.createResponse(ErrorCode.NORMAL,
                    Json.toJson(builder.get()));
        } catch (IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }

    /**
     * 通过昵称获得用户的详细信息。
     *
     * @param userName
     * @return
     */
    public static Result getUserProfileByName(String userName) throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("user_info");
        DBObject userItem = col.findOne(QueryBuilder.start("name").is(userName).get());
        if (userItem == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user name: %s.", userName));

        return Utils.createResponse(ErrorCode.NORMAL,
                Json.toJson(BasicDBObjectBuilder.start("_id", userItem.get("_id").toString()).add("name", userName)
                        .get()));
    }

    /**
     * 获得用户收藏的景点。
     *
     * @param userId
     * @return
     */
    public static Result getUserFavoredVS(String userId, int page, int pageSize) throws UnknownHostException {
        try {
            ObjectId uid = new ObjectId(userId);
            DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("favored_vs");
            DBCollection vsCol = Utils.getMongoClient("localhost", 27017).getDB("poi").getCollection("view_spot");
            DBCursor cursor = col.find(QueryBuilder.start("uid").is(uid).get()).skip(page * pageSize).limit(pageSize);
            BasicDBList results = new BasicDBList();
            while (cursor.hasNext()) {
                DBObject userItem = cursor.next();
                // 将景区的id和名称添加到返回结果中。
                DBObject vs = vsCol.findOne(userItem.get("vsId"), BasicDBObjectBuilder.start("name", 1).get());
                results.add(BasicDBObjectBuilder.start("_id", vs.get("_id").toString()).add("name", vs.get("name")).get());
            }

            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }

    /**
     * 添加对景区的收藏。
     *
     * @param userId
     * @return
     */
    public static Result putUserFavoredVS(String userId) throws UnknownHostException, TravelPiException {
        ObjectId vsId, uid;
        try {
            uid = new ObjectId(userId);
            DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("user_info");
            DBObject user = col.findOne(BasicDBObjectBuilder.start("_id", uid).get());
            if (user == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
            vsId = new ObjectId(request().body().asJson().get("_id").asText());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid request.");
        }

        DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("favored_vs");
        DBObject query = BasicDBObjectBuilder.start("uid", uid).add("vsId", vsId).get();
        DBObject item = col.findOne(query);
        if (item == null) {
            query.put("time", new Date());
            item = query;
            col.save(item);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
    }

    /**
     * 删除对景区的收藏。
     *
     * @param userId
     * @param vsId
     * @return
     */
    public static Result removeUserFavoredVS(String userId, String vsId) throws UnknownHostException, TravelPiException {
        ObjectId vsOid, userOid;
        try {
            userOid = new ObjectId(userId);
            vsOid = new ObjectId(vsId);
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid request.");
        }

        DBCollection col = Utils.getMongoClient("localhost", 27017).getDB("user").getCollection("favored_vs");
        col.remove(BasicDBObjectBuilder.start("uid", userOid).add("vsId", vsOid).get());

        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
    }


    /**
     * 根据设备号获得绑定的用户。如果该UDID不存在，则自动生成一个新用户，并绑定。
     *
     * @param udid
     * @return
     */
    public static Result getUserByUDID(String udid) throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("user").getCollection("user_info");
        DBObject user = col.findOne(BasicDBObjectBuilder.start("udid", udid).get());
        if (user == null) {
            // 用户没有找到，重新生成
            user = BasicDBObjectBuilder.start("_id", new ObjectId()).add("udid", udid).add("regTime", new Date()).get();
            col.save(user);
        }
        return getUserProfileById(user.get("_id").toString());
    }

    /**
     * 第三方登录
     *
     * @return
     */
    public static Result oauthLogin() {
        JsonNode req = request().body().asJson();
        String provider = req.get("provider").asText();
        String oauthId = req.get("oauthId").asText();
        String secToken = Utils.getSecToken();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"nickName", "avatar", "token", "udid"}) {
            try {
                builder.add(k, req.get(k).asText());
            } catch (NullPointerException ignored) {
            }
        }

        try {
            // TODO 如果已经存在，则更新相应信息，比如todo
            UserInfo user = UserAPI.regByOAuth(provider, oauthId, builder.get(), secToken);
            return Utils.createResponse(ErrorCode.NORMAL, user.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result udidLogin(String udid) {
        try {
            UserInfo user = UserAPI.regByUdid(udid);
            return Utils.createResponse(ErrorCode.NORMAL, user.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
