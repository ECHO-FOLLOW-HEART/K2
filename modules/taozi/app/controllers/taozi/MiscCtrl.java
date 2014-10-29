package controllers.taozi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.Feedback;
import models.misc.MiscInfo;
import models.misc.Recommendation;
import models.misc.TravelNote;
import models.poi.Hotel;
import models.poi.Restaurant;
import models.poi.ViewSpot;
import models.user.Favorite;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.taozi.user.SelfFavoriteFormatter;

import java.net.UnknownHostException;
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
            if (body == null || body.equals(""))
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "No body found.");
            Feedback feedBack = new Feedback();
            Datastore dsSave = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            feedBack.uid = uid;
            feedBack.body = body;
            feedBack.time = new Date();
            feedBack.enabled = true;
            dsSave.save(feedBack);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", feedback.get("userId").asText()));
        }

    }

    /**
     * 获取推荐信息
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    public static Result recommend(String type, int page, int pageSize) {
        JsonNode feedback = request().body().asJson();
        List<JsonNode> results = new ArrayList<>();
        Datastore ds;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recommendation> query = ds.createQuery(Recommendation.class);

            query.field("enabled").equal(Boolean.TRUE).field(type).greaterThan(0);
            query.order(type).offset(page * pageSize).limit(pageSize);

            for (Iterator<Recommendation> it = query.iterator(); it.hasNext(); ) {
                results.add(it.next().toJson());
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appRecommendFilter(Json.toJson(results), request()));
    }

    /**
     * 添加收藏
     *
     * @return
     */
    public static Result addFavorite(Integer userID) {

        JsonNode collection = request().body().asJson();
        try {
            String itemId = collection.get("itemId").asText();
            ObjectId oid = new ObjectId(itemId);
            String type = collection.get("type").asText();
            String name = collection.get("name").asText();
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Favorite fa = ds.createQuery(Favorite.class).field("userId").equal(userID).get();
            // TODO 还用判断用户么?
            if (fa == null)
                fa = createFavorite(userID);
            switch (type) {
                case "vs":
                    ViewSpot poi = new ViewSpot();
                    poi.id = oid;
                    poi.name = name;

                    fa.vs.add(poi);
                    break;
                case "hotel":

                    Hotel hotel = new Hotel();
                    hotel.id = oid;
                    hotel.name = name;

                    fa.hotel.add(hotel);
                    break;
                case "restaurant":
                    Restaurant res = new Restaurant();
                    res.id = oid;
                    res.name = name;

                    fa.restaurant.add(res);
                    break;
                case "shopping":

                    // TODO 缺少购物和美食
                    break;
                case "entertainment":

                    break;
                case "travelNote":
                    TravelNote tn = new TravelNote();
                    tn.id = oid;
                    tn.title = name;

                    fa.travelNote.add(tn);
                    break;
            }
            ds.save(fa);
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    private static Favorite createFavorite(Integer userID){
        Favorite fa = new Favorite();
        fa.id = new ObjectId();
        fa.userId = userID;
        fa.vs = new ArrayList<>();
        fa.hotel = new ArrayList<>();
        fa.restaurant = new ArrayList<>();
        return fa;
    }
    /**
     * 取得收藏
     *
     * @return
     */
    public static Result getFavorite(Integer userID) {

        try {
            Favorite fa = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).createQuery(Favorite.class).get();
            if (fa != null) {
                JsonNode info = new SelfFavoriteFormatter().format(fa);
                ObjectNode ret = (ObjectNode) info;
                return Utils.createResponse(ErrorCode.NORMAL, ret);
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userID));
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 删除收藏
     *
     * @return
     */
    public static Result delFavorite(Integer userID) {

        JsonNode fav = request().body().asJson();
        try {
            String itemId = fav.get("itemId").asText();
            ObjectId oid = new ObjectId(itemId);
            String type = fav.get("type").asText();
            String name = fav.get("name").asText();
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Favorite fa = ds.createQuery(Favorite.class).field("userId").equal(userID).get();
            if (fa == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userID));
            Query<Favorite> query = ds.createQuery(Favorite.class);
            switch (type) {
                case "vs":
                    ViewSpot poi = new ViewSpot();
                    poi.id = oid;
                    poi.name = name;

                    fa.vs.add(poi);
                    break;
                case "hotel":
                    Hotel hotel = new Hotel();
                    hotel.id = oid;
                    hotel.name = name;

                    fa.hotel.add(hotel);
                    break;
                case "restaurant":
                    Restaurant res = new Restaurant();
                    res.id = oid;
                    res.name = name;

                    fa.restaurant.add(res);
                    break;
                case "shopping":

                    // TODO 缺少购物和美食
                    break;
                case "entertainment":

                    break;
                case "travelNote":
                    TravelNote tn = new TravelNote();
                    tn.id = oid;
                    tn.title = name;

                    fa.travelNote.add(tn);
                    break;
            }
            if (query.iterator().hasNext()) {
                ds.delete(query);
                return Utils.createResponse(ErrorCode.NORMAL, "Success.");
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Favorite id: %s.", oid));
        } catch (NullPointerException | IllegalArgumentException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }
}
