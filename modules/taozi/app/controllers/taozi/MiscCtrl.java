package controllers.taozi;

import aizou.core.WeatherAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.*;
import models.user.Favorite;
import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.LogUtils;
import utils.MsgConstants;
import utils.Utils;
import utils.formatter.taozi.misc.WeatherFormatter;
import utils.formatter.taozi.recom.RecomFormatter;
import utils.formatter.taozi.recom.RecomTypeFormatter;
import utils.formatter.taozi.user.SelfFavoriteFormatter;

import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 其它
 *
 * @author Zephyre
 */
public class MiscCtrl extends Controller {

    /**
     * 封面故事,获取App首页的图像。
     *
     * @param width  指定宽度
     * @param height 指定高度
     * @return
     */
    public static Result appHomeImage(int width, int height, int quality, String format, int interlace) {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            MiscInfo info = ds.createQuery(MiscInfo.class).field("application").equal(Constants.APP_FLAG_PEACH).get();
            if (info == null)
                return Utils.createResponse(ErrorCode.UNKOWN_ERROR, Json.newObject());
            ObjectNode node = Json.newObject();
            // 示例：http://zephyre.qiniudn.com/misc/Kirkjufellsfoss_Sunset_Iceland5.jpg?imageView/1/w/400/h/200/q/85/format/webp/interlace/1
            String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", info.appHomeImage, width, height, quality, format, interlace);
            //添加封面故事信息
            for (Map.Entry<String, String> entry : info.coverStory.entrySet()) {
                node.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
            node.put("image", url);
            node.put("width", width);
            node.put("height", height);
            node.put("fmt", format);
            node.put("quality", quality);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result postFeedback() throws UnknownHostException, TravelPiException {
        JsonNode feedback = request().body().asJson();
        try {
            Integer uid = feedback.has("userId") ? feedback.get("userId").asInt() : null;
            String body = feedback.has("body") ? feedback.get("body").asText().trim() : null;
            if (body == null || body.equals("") || uid == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid feedback content.");
            Feedback feedBack = new Feedback();
            Datastore dsSave = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            feedBack.uid = uid;
            feedBack.body = body;
            feedBack.time = new Date();
            feedBack.setEnabled(true);
            dsSave.save(feedBack);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", feedback.get("userId").asText()));
        }

    }

    /**
     * 获取推荐信息
     *
     * @return
     */
    public static Result recommend(int page, int pageSize) {
        List<ObjectNode> retNodeList = new ArrayList();
        Datastore ds;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recom> query = ds.createQuery(Recom.class);

            query.field("enabled").equal(Boolean.TRUE);
            query.order("weight").offset(page * pageSize).limit(pageSize);
            Recom recom;
            Map<ObjectId, List<Recom>> map = new HashMap<>();
            Map<ObjectId, RecomType> typeMap = new HashMap<>();
            List<Recom> tempList;
            for (Iterator<Recom> it = query.iterator(); it.hasNext(); ) {
                recom = it.next();
                tempList = map.get(recom.type.getId());
                if (tempList == null)
                    tempList = new ArrayList<>();
                tempList.add(recom);
                map.put(recom.type.getId(), tempList);
                typeMap.put(recom.type.getId(), recom.type);
            }
            ObjectId key;
            ObjectNode tempNode;
            List<Recom> recList;
            List<ObjectNode> recNodeList;
            for (Map.Entry<ObjectId, List<Recom>> entry : map.entrySet()) {
                key = entry.getKey();
                recList = entry.getValue();
                recNodeList = new ArrayList();
                for (Recom tem : recList) {
                    recNodeList.add((ObjectNode) new RecomFormatter().format(tem));
                }
                tempNode = Json.newObject();
                tempNode.put("type", new RecomTypeFormatter().format(typeMap.get(key)));
                tempNode.put("localities", Json.toJson(recNodeList));
                retNodeList.add(tempNode);
            }

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(retNodeList));
    }

    /**
     * 添加收藏
     *
     * @return
     */
    public static Result addFavorite() {

        JsonNode collection = request().body().asJson();
        try {
            Integer userId = Integer.parseInt(request().getHeader("UserId"));
            String itemId = collection.get("itemId").asText();
            ObjectId oid = new ObjectId(itemId);
            String type = collection.get("type").asText();
            String zhName = collection.get("zhName").asText();
            String enName = collection.get("enName").asText();
            String image = collection.get("image").asText();
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<Favorite> query = ds.createQuery(Favorite.class);
            query.field("userId").equal("userId").field("type").equal(type).field("itemId").equal(oid);
            if (query.iterator().hasNext())
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Favorite item has existed");
            Favorite fa = new Favorite();
            fa.setId(new ObjectId());
            fa.itemId = oid;
            fa.zhName = zhName;
            fa.enName = enName;
            fa.type = type;
            ImageItem img = new ImageItem();
            img.url = image;
            fa.images = Arrays.asList(img);
            fa.userId = userId;
            fa.createTime = new Date();
            ds.save(fa);
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得收藏
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Result getFavorite(String faType, int page, int pageSize) {

        try {
            Integer userId = Integer.parseInt(request().getHeader("UserId"));
            Query<Favorite> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).createQuery(Favorite.class);
            query.field("userId").equal(userId);
            if (faType.equals("all")||faType.equals("")) {
                List<CriteriaContainerImpl> criList = new ArrayList<>();
                List<String> allTypes = Arrays.asList(Favorite.TYPE_VS, Favorite.TYPE_HOTEL, Favorite.TYPE_TRAVELNOTE, Favorite.TYPE_SHOPPING
                        , Favorite.TYPE_RESTAURANT, Favorite.TYPE_LOCALITY, Favorite.TYPE_ENTERTAINMENT);
                for (String fd : allTypes)
                    criList.add(query.criteria("type").equal(fd));
                query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
            } else {
                query.field("type").equal(faType);
            }
            query.offset(page * pageSize).limit(pageSize);
            query.order("-" + "createTime");
            Favorite fa = query.get();
            if (fa != null) {
                JsonNode info = new SelfFavoriteFormatter().format(fa);
                ObjectNode ret = (ObjectNode) info;
                return Utils.createResponse(ErrorCode.NORMAL, ret);
            } else
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, MsgConstants.FAVORITE_NOT_EXIT_MSG, true);
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 删除收藏
     *
     * @return
     */
    public static Result delFavorite(String id) {
        try {
            ObjectId oid = new ObjectId(id);
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<Favorite> query = ds.createQuery(Favorite.class).field("id").equal(oid);
            ds.delete(query);
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 通过城市id获得天气情况
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static Result getWeatherDetail(String id) {
        try {
            YahooWeather weather = WeatherAPI.weatherDetails(new ObjectId(id));
            return Utils.createResponse(ErrorCode.NORMAL, new WeatherFormatter().format(weather));
        } catch (NullPointerException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 获得资源上传凭证
     *
     * @param scenario 上传场景: PORTRAIT-上传头像
     * @return
     */
    public static Result putPolicy(String scenario) {

        Configuration config = Configuration.root();
        try {
            String userId = request().getHeader("UserId");
            String picName = getPicName(userId);
            Map qiniu = (Map) config.getObject("qiniu");
            String secretKey = qiniu.get("secertKey").toString();
            String accessKey = qiniu.get("accessKey").toString();
            String scope, callbackUrl;
            if (scenario.equals("portrait")) {
                scope = qiniu.get("avaterScope").toString();
                callbackUrl = qiniu.get("callbackUrl").toString();
                callbackUrl = "http://" + callbackUrl;
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid scenario.");
            //取得上传策略
            ObjectNode policy = getPutPolicyInfo(scope, picName, callbackUrl);
            // UrlBase64编码
            String encodedPutPolicy = Base64.encodeBase64URLSafeString(policy.toString().trim().getBytes());
            encodedPutPolicy = Utils.base64Padding(encodedPutPolicy);
            // 构造密钥并UrlBase64编码
            String encodedSign = Base64.encodeBase64URLSafeString(Utils.hmac_sha1(encodedPutPolicy, secretKey));
            encodedSign = Utils.base64Padding(encodedSign);

            StringBuffer uploadToken = new StringBuffer(10);
            uploadToken.append(accessKey);
            uploadToken.append(":");
            uploadToken.append(encodedSign);
            uploadToken.append(":");
            uploadToken.append(encodedPutPolicy);

            ObjectNode ret = Json.newObject();
            ret.put("uploadToken", uploadToken.toString());
            ret.put("key", picName);
            return Utils.createResponse(ErrorCode.NORMAL, ret);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {

            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 回调方式，上传用户头像，上传策略序列化为JSON
     *
     * @return
     */
    private static ObjectNode getPutPolicyInfo(String scope, String picName, String callbackUrl) {

        ObjectNode info = Json.newObject();
        info.put("scope", scope + ":" + picName);
        info.put("deadline", System.currentTimeMillis() / 1000 + 2 * 3600);
        info.put("callBackBody", getCallBackBody());
        info.put("callbackUrl", callbackUrl);
        return info;
    }

    /**
     * 定义回调Body
     *
     * @return
     */
    private static String getCallBackBody() {
        StringBuffer callbackBody = new StringBuffer(10);
        callbackBody.append("name=$(fname)");
        callbackBody.append("&size=$(fsize)");
        callbackBody.append("&h=$(imageInfo.height)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&hash=$(etag)");
        return callbackBody.toString();
    }

    private static String getReturnBody() {
        ObjectNode info = Json.newObject();
        info.put("name", "$(fname)");
        info.put("size", "$(fsize)");
        info.put("w", "$(imageInfo.width)");
        info.put("h", "$(imageInfo.height)");
        info.put("hash", "$(etag)");
        return info.toString();
    }

    /**
     * 取得文件名
     *
     * @param userId
     * @return
     */
    public static String getPicName(String userId) {
        Date date = new Date();
        return "avt_" + userId + date.getTime() + ".jpg";
    }

    /**
     * 上传回调
     *
     * @return
     */
    public static Result getCallback() {
        Map<String, String[]> fav = request().body().asFormUrlEncoded();
        ObjectNode ret = Json.newObject();
        for (Map.Entry<String, String[]> entry : fav.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            // TODO 日志
            LogUtils.info(MiscCtrl.class, key + value);
            ret.put(key, value[0]);
        }
        ret.put("success", true);
        return ok(ret);
    }

}
