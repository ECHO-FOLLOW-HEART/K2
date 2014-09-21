package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.LocalityAPI;
import core.PoiAPI;
import core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import models.morphia.misc.Feedback;
import models.morphia.misc.MiscInfo;
import models.morphia.misc.Recommendation;
import models.morphia.poi.AbstractPOI;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
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
            //DBCollection col = Utils.getMongoClient().getDB("user").getCollection("user_info");
            //DBObject userItem = col.findOne(QueryBuilder.start("_id").is(uid).get());
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<UserInfo> query = ds.createQuery(UserInfo.class);
            query.field("_id").equal(uid);
            if (!query.iterator().hasNext())
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

        Feedback feedBack = new Feedback();
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        feedBack.uid = uid;
        feedBack.body = body;
        feedBack.time = new Date();
        feedBack.enabled = true;
        ds.save(feedBack);
//        String title = null;
//        if (feedback.has("title"))
//            title = feedback.get("title").asText();
//        DBObject entry = new BasicDBObject();
//        if (uid != null)
//            entry.put("user", uid);
//        entry.put("body", body);
//        entry.put("time", new Date());

//        JsonNode contact = feedback.get("contact");
//        if (contact != null)
//            entry.put("contact", JSON.parse(contact.toString()));
        //MongoClient client = Utils.getMongoClient();
        //DBCollection col = client.getDB("misc").getCollection("feedback");
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }


    /**
     * 获得更新信息
     *
     * @return
     */
    public static Result getUpdateInfo(String platform) {
        return getUpdates();
    }


    /**
     * 获得更新信息
     *
     * @return
     */
    public static Result getUpdates() {
        Http.Request req = request();
        String platform = req.getQueryString("platform");
        if (platform == null)
            // 在1.0版本中，
            platform = req.getQueryString("platformVer");

        String ver = req.getQueryString("v");

        if (ver == null || ver.isEmpty() || platform == null || platform.isEmpty())
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID ARGUMENT");

        platform = platform.toLowerCase();
        ver = ver.toLowerCase();

        double oldVerN = 0;
        String[] oldVerP = ver.split("\\.");
        for (int i = 0; i < oldVerP.length; i++)
            oldVerN += Math.pow(10, -3 * i) * Double.parseDouble(oldVerP[i]);

        Matcher m = Pattern.compile("^(android|ios)").matcher(platform);
        if (!m.find() || !m.group(1).equals("android"))
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("INVALID PLATFORM: %s", platform));

        BasicDBObject ret;
        try {
            DBCollection col = Utils.getMongoClient().getDB("misc").getCollection("MiscInfo");
            ret = (BasicDBObject) col.findOne();
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        String newVerS = ret.getString("androidUpdates");
        String[] newVerP = newVerS.split("\\.");
        double newVerN = 0;
        for (int i = 0; i < newVerP.length; i++)
            newVerN += Math.pow(10, -3 * i) * Double.parseDouble(newVerP[i]);

        ObjectNode result = Json.newObject();
        if (newVerN > oldVerN) {
            result.put("update", true);
            result.put("version", newVerS);
            result.put("desc", "");
            result.put("downloadUrl", ret.getString("androidUrl"));
        } else
            result.put("update", false);
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }


    /**
     * 根据搜索词获得提示
     *
     * @param word
     * @param pageSize
     * @return
     */
    public static Result getSuggestions(String word, int loc, int vs, int hotel, int restaurant, int pageSize) {
        ObjectNode ret = Json.newObject();

        try {
            List<JsonNode> locList = new ArrayList<>();
            if (loc != 0) {
                for (Iterator<Locality> it = LocalityAPI.getSuggestion(word, pageSize); it.hasNext(); ) {
                    // 如果locality为北京、上海、天津、重庆这四个直辖市，则忽略level=1的省级行政区
                    Locality item = it.next();
                    switch (item.zhName) {
                        case "北京市":
                        case "上海市":
                        case "重庆市":
                        case "天津市":
                        case "香港特别行政区":
                        case "澳门特别行政区":
                            if (item.level == 1)
                                continue;
                    }
                    locList.add(item.toJson(1));
                }
            }
            if (!locList.isEmpty())
                ret.put("loc", Json.toJson(locList));
            else
                ret.put("loc", Json.toJson(new ArrayList<>()));

            List<JsonNode> vsList = new ArrayList<>();
            if (vs != 0) {
                for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.VIEW_SPOT, word, pageSize);
                     it.hasNext(); )
                    vsList.add(it.next().toJson(1));
            }
            if (!vsList.isEmpty())
                ret.put("vs", Json.toJson(vsList));
            else
                ret.put("vs", Json.toJson(new ArrayList<>()));

            List<JsonNode> hotelList = new ArrayList<>();
            if (hotel != 0) {
                for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.HOTEL, word, pageSize);
                     it.hasNext(); )
                    hotelList.add(it.next().toJson(1));
            }
            if (!hotelList.isEmpty())
                ret.put("hotel", Json.toJson(hotelList));
            else
                ret.put("hotel", Json.toJson(new ArrayList<>()));

            List<JsonNode> dinningList = new ArrayList<>();
            if (restaurant != 0) {
                for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.RESTAURANT, word, pageSize);
                     it.hasNext(); )
                    dinningList.add(it.next().toJson(1));
            }
            if (!dinningList.isEmpty())
                ret.put("restaurant", Json.toJson(dinningList));
            else
                ret.put("restaurant", Json.toJson(new ArrayList<>()));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(ret));
    }

    /**
     * 根据搜索词获得提示
     *
     * @param word
     * @param pageSize
     * @return
     * @throws UnknownHostException
     */
    public static Result getSuggestionsOld(String word, int loc, int vs, int hotel, int restaurant, int pageSize) throws UnknownHostException, TravelPiException {
        int y = 0;
        ObjectNode ret = Json.newObject();
        if (loc != 0) {
            DBObject extra = BasicDBObjectBuilder.start("level", BasicDBObjectBuilder.start("$gte", 1).get()).get();
            List<JsonNode> locSug = getSpecSug(word, pageSize, "zhName", "geo", "locality", extra);
            ret.put("loc", Json.toJson(locSug));
        }
        if (vs != 0)
            ret.put("vs", Json.toJson(PoiAPI.getSuggestionsOld(PoiAPI.POIType.VIEW_SPOT, word, 0, pageSize)));
        if (hotel != 0)
            ret.put("hotel", Json.toJson(PoiAPI.getSuggestionsOld(PoiAPI.POIType.HOTEL, word, 0, pageSize)));
        if (restaurant != 0)
            ret.put("restaurant", Json.toJson(PoiAPI.getSuggestionsOld(PoiAPI.POIType.RESTAURANT, word, 0, pageSize)));
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
    public static Result explore(int details, int loc, int vs, int hotel, int restaurant, int page, int pageSize) throws TravelPiException {
        boolean detailsFlag = (details != 0);
        ObjectNode results = Json.newObject();

        // 发现城市
        if (loc != 0) {
            List<JsonNode> retLocList = new ArrayList<>();
            // TODO 获得城市信息
            for (Locality locality : LocalityAPI.explore(detailsFlag, page, pageSize))
                retLocList.add(locality.toJson(2));
            results.put("loc", Json.toJson(retLocList));
        }

        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
        if (vs != 0)
            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
        if (hotel != 0)
            poiKeyList.add(PoiAPI.POIType.HOTEL);
        if (restaurant != 0)
            poiKeyList.add(PoiAPI.POIType.RESTAURANT);

        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<PoiAPI.POIType, String>() {
            {
                put(PoiAPI.POIType.VIEW_SPOT, "vs");
                put(PoiAPI.POIType.HOTEL, "hotel");
                put(PoiAPI.POIType.RESTAURANT, "restaurant");
            }
        };

        for (PoiAPI.POIType poiType : poiKeyList) {
            if (poiType == PoiAPI.POIType.VIEW_SPOT) {
                // TODO 暂时不返回景点推荐数据
                results.put(poiMap.get(poiType), Json.toJson(new ArrayList<>()));
            } else {
                // 发现POI
                List<JsonNode> retPoiList = new ArrayList<>();
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, (ObjectId) null, page, pageSize);
                     it.hasNext(); )
                    retPoiList.add(it.next().toJson(2));
                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
            }
        }

        return Utils.createResponse(ErrorCode.NORMAL, results);
    }

    /**
     * 更新用户信息。
     *
     * @return
     */
    public static Result updateUserInfo() {
        try {

            UserAPI.updateUserInfo(request());

            return Utils.createResponse(ErrorCode.NORMAL, "Update UserInfo Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 获得App首页的图像。
     *
     * @param width  指定宽度
     * @param height 指定高度
     * @return
     */
    public static Result appHomeImage(int width, int height, int quality, String format, int interlace) {
        try {

            //UserAPI.updateUserInfo(request());

            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            MiscInfo info = ds.createQuery(MiscInfo.class).get();

            if (info == null)
                return Utils.createResponse(ErrorCode.UNKOWN_ERROR, Json.newObject());

            ObjectNode node = Json.newObject();
            // 示例：http://zephyre.qiniudn.com/misc/Kirkjufellsfoss_Sunset_Iceland5.jpg?imageView/1/w/400/h/200/q/85/format/webp/interlace/1
            String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", info.appHomeImage, width, height, quality, format, interlace);
            node.put("image", url);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result getWeatherInfo(String locId) {
        try {

            //请求验证
            String sign = request().getQueryString("sign");
            String uid = request().getQueryString("uid");
            String timestamp = request().getQueryString("timestamp");
            boolean auth = UserAPI.authenticate(uid, timestamp, sign);
            if (!auth)
                return Utils.createResponse(ErrorCode.AUTHENTICATE_ERROR, "AUTHENTIFICATION FAILED.");

            DBCollection colLoc = Utils.getMongoClient().getDB("misc").getCollection("Weather");
            DBObject qb = QueryBuilder.start("loc.id").is(new ObjectId(locId)).get();
            DBObject data = colLoc.findOne(qb);
            if (data == null) {
                Locality loc = LocalityAPI.locDetails(locId, 1);
                if (loc == null)
                    return Utils.createResponse(ErrorCode.UNKOWN_ERROR, String.format("CANNOT FIND LOCALITY: %s.", locId));
                data = colLoc.findOne(QueryBuilder.start("loc.id").is(loc.superAdm.id).get());
            }
            if (data == null)
                return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "NO RESULTS FOUND.");

            ObjectNode result = Json.newObject();

            DBObject loc = (DBObject) data.get("loc");
            Object tmp = loc.get("zhName");
            result.put("locName", tmp != null ? tmp.toString() : "");
            tmp = loc.get("id");
            result.put("locId", tmp != null ? tmp.toString() : "");

            BasicDBList ret = (BasicDBList) data.get("weather_data");
            DBObject entry = (ret.size() > 0 ? (DBObject) ret.get(0) : new BasicDBObject());
            tmp = entry.get("temperature");
            result.put("temperature", tmp != null ? tmp.toString() : "");
            tmp = entry.get("dayPictureUrl");
            Matcher m = Pattern.compile("(\\w+)\\.png$").matcher(tmp != null ? tmp.toString() : "");

            String icon = (m.find() ? m.group(1) : "");
            int weatherType;
            switch (icon) {
                case "duoyun":
                    weatherType = 1;
                    break;
                case "yin":
                    weatherType = 2;
                    break;
                case "zhenyu":
                    weatherType = 3;
                    break;
                case "xiaoyu":
                    weatherType = 4;
                    break;
                case "zhongyu":
                    weatherType = 5;
                    break;
                case "dayu":
                    weatherType = 6;
                    break;
                case "baoyu":
                    weatherType = 7;
                    break;
                case "leizhenyu":
                    weatherType = 8;
                    break;
                case "wu":
                    weatherType = 9;
                    break;
                case "xiaoxue":
                    weatherType = 10;
                    break;
                case "zhongxue":
                    weatherType = 11;
                    break;
                case "daxue":
                    weatherType = 12;
                    break;
                case "baoxue":
                    weatherType = 13;
                    break;
                case "yujiaxue":
                    weatherType = 14;
                    break;
                case "qing":
                    weatherType = 15;
                    break;
                default:
                    weatherType = -1;
                    break;
            }
            result.put("icon", icon.isEmpty() ? "" : String.format("http://lxp-assets.qiniudn.com/weather/%s", icon));
            result.put("weatherType", weatherType);

            tmp = entry.get("weather");
            result.put("weather", tmp != null ? tmp.toString() : "");
            tmp = entry.get("date");
            result.put("sampleTime", tmp != null ? tmp.toString() : "");
            return Utils.createResponse(ErrorCode.NORMAL, result);

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result recommend(String type, int page, int pageSize) {
        List<JsonNode> results = new ArrayList<JsonNode>();

        Datastore ds = null;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recommendation> query = ds.createQuery(Recommendation.class);

            query.field("enabled").equal(Boolean.TRUE).field(type).notEqual(null);
            query.order(type).offset(page * pageSize).limit(pageSize);

            for (Iterator<Recommendation> it = query.iterator(); it.hasNext(); ) {
                results.add(it.next().toJson());
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }
}