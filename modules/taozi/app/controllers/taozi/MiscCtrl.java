package controllers.taozi;

import aizou.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.CacheKey;
import controllers.UsingCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.geo.DetailedLocalityFormatter;
import formatter.taozi.misc.*;
import formatter.taozi.poi.DetailedPOIFormatter;
import formatter.taozi.recom.RecomFormatter;
import formatter.taozi.user.SelfFavoriteFormatter;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.*;
import models.poi.AbstractPOI;
import models.poi.Comment;
import models.user.Favorite;
import models.user.UserInfo;
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
import utils.Utils;

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

    public static String UPLOAD_URL = "url";

    public static String UPLOAD_UID = "userId";

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
            MiscInfo info = ds.createQuery(MiscInfo.class).field("application").equal(Constants.APP_FLAG_TAOZI).get();
            if (info == null)
                return Utils.createResponse(ErrorCode.UNKOWN_ERROR, Json.newObject());
            ObjectNode node = Json.newObject();
            // 示例：http://zephyre.qiniudn.com/misc/Kirkjufellsfoss_Sunset_Iceland5.jpg?imageView/1/w/400/h/200/q/85/format/webp/interlace/1
            String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", info.appHomeImage, width, height, quality, format, interlace);
            //添加封面故事信息
            if (info.coverStory != null)
                for (Map.Entry<String, String> entry : info.coverStory.entrySet()) {
                    node.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
                }
            node.put("image", url);
            node.put("width", width);
            node.put("height", height);
            node.put("fmt", format);
            node.put("quality", quality);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    public static Result postFeedback() throws UnknownHostException, AizouException {
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
        } catch (NullPointerException | IllegalArgumentException | AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", feedback.get("userId").asText()));
        }

    }

    /**
     * 获取推荐信息
     *
     * @return
     */
    public static Result recommend(int page, int pageSize) {
        List<ObjectNode> retNodeList = new ArrayList<ObjectNode>();
        Datastore ds;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recom> query = ds.createQuery(Recom.class);

            query.field("enabled").equal(Boolean.TRUE);
            query.order("weight").offset(page * pageSize).limit(pageSize);
            Recom recom;
            Map<String, List<Recom>> map = new HashMap<>();
            List<Recom> tempList;
            for (Iterator<Recom> it = query.iterator(); it.hasNext(); ) {
                recom = it.next();
                tempList = map.get(recom.title);
                if (tempList == null)
                    tempList = new ArrayList<>();
                tempList.add(recom);
                map.put(recom.title, tempList);
            }
            String key;
            ObjectNode tempNode;
            List<Recom> recList;
            List<ObjectNode> recNodeList;
            for (Map.Entry<String, List<Recom>> entry : map.entrySet()) {
                key = entry.getKey();
                recList = entry.getValue();
                recNodeList = new ArrayList<ObjectNode>();
                for (Recom tem : recList) {
                    recNodeList.add((ObjectNode) new RecomFormatter().format(tem));
                }
                tempNode = Json.newObject();
                tempNode.put("title", key == null ? "" : key);
                tempNode.put("contents", Json.toJson(recNodeList));
                retNodeList.add(tempNode);
            }

        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
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
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<Favorite> query = ds.createQuery(Favorite.class);
            query.field("userId").equal(userId).field("type").equal(type).field("itemId").equal(oid);
            if (query.iterator().hasNext())
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "Favorite item has existed");
            Favorite fa = new Favorite();
            fa.setId(new ObjectId());
            fa.itemId = oid;
            fa.type = type;
            fa.userId = userId;
            fa.createTime = new Date();
            ds.save(fa);
        } catch (NullPointerException | IllegalArgumentException | AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得收藏
     *
     * @return
     */
    public static Result getFavorite(String faType, int page, int pageSize) {

        try {
            Integer userId = Integer.parseInt(request().getHeader("UserId"));
            Query<Favorite> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).createQuery(Favorite.class);
            query.field("userId").equal(userId);
            if (faType.equals("all") || faType.equals("")) {
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

            List<Favorite> faList = query.asList();
            if (faList == null || faList.isEmpty())
                return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(new ArrayList<Favorite>()));
            List<Favorite> faShowList = new ArrayList<>();
            List<Favorite> noteFav = new ArrayList<>();
            List<String> travelNoteIds = new ArrayList<>();
            String type;
            Locality loc;
            AbstractPOI poi;
            PoiAPI.POIType poiType;
            List<String> locFields = new ArrayList<String>();
            Collections.addAll(locFields, "id", "zhName", "enName", "images", "desc", "timeCostDesc");
            List<String> poiFields = new ArrayList<String>();
            Collections.addAll(poiFields, "id", "zhName", "enName", "images", "desc", "type", "locality", "address", "price");
            for (Favorite fa : faList) {
                type = fa.type;
                if (type.equals("locality")) {
                    loc = GeoAPI.locDetails(fa.itemId, locFields);
                    if (loc == null)
                        continue;
                    fa.zhName = loc.getZhName();
                    fa.enName = loc.getEnName();
                    fa.images = loc.getImages();
                    fa.desc = loc.getDesc();
                    // 城市显示建议游玩时间
                    fa.timeCostDesc = loc.getTimeCostDesc();
                } else if (type.equals("travelNote")) {
                    travelNoteIds.add(fa.itemId.toString());
                    noteFav.add(fa);
                } else if (type.equals("vs") || type.equals("hotel") || type.equals("restaurant") || type.equals("shopping")) {
                    switch (type) {
                        case "vs":
                            poiType = PoiAPI.POIType.VIEW_SPOT;
                            break;
                        case "hotel":
                            poiType = PoiAPI.POIType.HOTEL;
                            break;
                        case "restaurant":
                            poiType = PoiAPI.POIType.RESTAURANT;
                            break;
                        case "shopping":
                            poiType = PoiAPI.POIType.SHOPPING;
                            break;
                        default:
                            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", type));
                    }
                    poi = PoiAPI.getPOIInfo(fa.itemId, poiType, poiFields);
                    if (poi == null)
                        continue;
                    fa.zhName = poi.zhName;
                    fa.enName = poi.enName;
                    fa.images = poi.images;
                    fa.desc = poi.desc;
                    fa.locality = poi.getLocality();
                    fa.timeCostDesc = poi.timeCostDesc;
                    fa.priceDesc = poi.priceDesc;
                    fa.rating = poi.rating;
                    fa.address = poi.address;
                    fa.telephone = poi.telephone;
                }
                faShowList.add(fa);
            }
            // TODO 如果type不等于travelNote,则一下语句会抛出NullPointerException异常
//            List<TravelNote> travelNotes = TravelNoteAPI.searchNoteById(travelNoteIds, Constants.MAX_COUNT);
//            Map<String, TravelNote> travelNoteMap = new HashMap<>();
//            for (TravelNote temp : travelNotes)
//                travelNoteMap.put(temp.getId().toString(), temp);
//            TravelNote tnFromFavorate;
//            if (travelNotes != null && travelNotes.size() > 0) {
//                for (Favorite fa : noteFav) {
//                    tnFromFavorate = travelNoteMap.get(fa.itemId.toString());
//                    if (tnFromFavorate == null)
//                        continue;
//                    fa.zhName = tnFromFavorate.getName();
//                    fa.enName = tnFromFavorate.getName();
//                    ImageItem tmg = new ImageItem();
//                    TODO 如何设置URL
                    //tmg.setKey(tnFromFavorate.getCover());
//                    fa.images = Arrays.asList(tmg);
//                    fa.desc = tnFromFavorate.getDesc();
//                    faShowList.add(fa);
//                }
//            }
            // 按照创建时间排序
            Collections.sort(faShowList, new Comparator<Favorite>() {
                public int compare(Favorite arg0, Favorite arg1) {
                    return arg0.createTime.getTime() - arg1.createTime.getTime() > 0 ? -1 : 1;
                }
            });
            List<ObjectNode> nodes = new ArrayList<>();
            for (Favorite fa : faShowList)
                nodes.add((ObjectNode) new SelfFavoriteFormatter(fa.type).format(fa));
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodes));
        } catch (NullPointerException | IllegalArgumentException | AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getLocalizedMessage());
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
            Integer userId = Integer.parseInt(request().getHeader("UserId"));
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<Favorite> query = ds.createQuery(Favorite.class).field("userId").equal(userId).field("itemId").equal(oid);
            ds.delete(query);
        } catch (NullPointerException | IllegalArgumentException | AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 通过城市id获得天气情况
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static Result getWeatherDetail(String id) {
        try {
            YahooWeather weather = WeatherAPI.weatherDetails(new ObjectId(id));
            return Utils.createResponse(ErrorCode.NORMAL, new WeatherFormatter().format(weather));
        } catch (NullPointerException | AizouException e) {
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
                scope = qiniu.get("taoziAvaterScope").toString();
                callbackUrl = qiniu.get("callbackUrl").toString();
                callbackUrl = "http://" + callbackUrl;
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid scenario.");
            //取得上传策略
            ObjectNode policy = getPutPolicyInfo(scope, picName, callbackUrl, Integer.valueOf(userId));
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
    private static ObjectNode getPutPolicyInfo(String scope, String picName, String callbackUrl, Integer userId) {

        ObjectNode info = Json.newObject();
        info.put("scope", scope + ":" + picName);
        info.put("deadline", System.currentTimeMillis() / 1000 + 2 * 3600);
        info.put("callBackBody", getCallBackBody(userId));
        info.put("callbackUrl", callbackUrl);
        return info;
    }

    /**
     * 定义回调Body
     *
     * @return
     */
    private static String getCallBackBody(Integer userId) {
        StringBuffer callbackBody = new StringBuffer(10);
        callbackBody.append("name=$(fname)");
        callbackBody.append("&size=$(fsize)");
        callbackBody.append("&h=$(imageInfo.height)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&hash=$(etag)");
        callbackBody.append("&bucket=$(bucket)");
        String url = "http://" + "$(bucket)" + ".qiniudn.com" + Constants.SYMBOL_SLASH + "$(key)";
        // 定义图片的URL
        callbackBody.append("&" + UPLOAD_URL + "=" + url);
        // 定义用户ID
        callbackBody.append("&" + UPLOAD_UID + "=" + userId);
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
        String url = null;
        String userId = null;
        for (Map.Entry<String, String[]> entry : fav.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            //LogUtils.info(MiscCtrl.class, key + "&&" + value[0]);
            if (key.equals(UPLOAD_URL))
                url = value[0];
            if (key.equals(UPLOAD_UID))
                userId = value[0];
            ret.put(key, value[0]);
        }
        ret.put("success", true);
        try {
            UserAPI.resetAvater(Integer.valueOf(userId), url);
        } catch (AizouException e) {
            LogUtils.info(MiscCtrl.class, "Update user avatar error.");
        }
        return ok(ret);
    }


    /**
     * 旅行专栏
     *
     * @return
     */
    @UsingCache(key = "columns", expireTime = 20)
    public static Result getColumns() {
        MiscFormatter formatter = new MiscFormatter();
        try {
            List<JsonNode> columns = new ArrayList<>();
            for (Column c : MiscAPI.getColumns()) {
                columns.add(formatter.format(c));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(columns));
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 保存用户的评论
     *
     * @return
     */
    public static Result saveComment(String poiId) {
        Comment comment = new Comment();

        try {
            JsonNode req = request().body().asJson();
            String userId = request().getHeader("UserId");
            Long userIdLong = Long.parseLong(userId);
            UserInfo user = UserAPI.getUserInfo(userIdLong,
                    Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar));
            if (user != null) {
                comment.setUserName(user.getNickName());
                comment.setUserAvatar(user.getAvatar());
                comment.setUserId(userIdLong);
            } else {
                throw new AizouException(ErrorCode.USER_NOT_EXIST);
            }

            Double rating = req.get("rating").asDouble();
            String contents = req.get("contents").asText();

            comment.setItemId(new ObjectId(poiId));
            comment.setContents(contents);
            comment.setRating(rating);
            long commentTime = System.currentTimeMillis();
            comment.setcTime(commentTime);
            comment.setmTime(commentTime);

            JsonNode result = MiscAPI.saveComment(comment);
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    private static JsonNode getCommentsImpl(String poiId, double lower, double upper, long lastUpdate, int pageSize)
            throws AizouException {
        CommentFormatter formatter = new CommentFormatter();
        List<Comment> commentList = MiscAPI.displayCommentApi(new ObjectId(poiId), lower, upper, lastUpdate, pageSize);
        List<JsonNode> list = new ArrayList<>();
        for (Comment comment : commentList)
            list.add(formatter.format(comment));
        return Json.toJson(list);
    }

    /**
     * 显示评论信息
     *
     * @param poiId
     * @param lastUpdate
     * @param pageSize
     * @return
     */
    public static Result displayComment(String poiId, double lower, double upper, long lastUpdate, int pageSize) {
        try {
            JsonNode results = getCommentsImpl(poiId, lower, upper, lastUpdate, pageSize);
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 联合查询
     *
     * @param keyWord
     * @param locId
     * @param loc
     * @param vs
     * @param hotel
     * @param restaurant
     * @param shopping
     * @param page
     * @param pageSize
     * @return
     */
//    @UsingCache(key = "search,keyWord=%0,locId={locId},loc=%2,vs=%3,hotel=%4,restaurant=%5,shopping={shopping},page=%7,pageSize=%8",
//                expireTime = 5)
    public static Result search(@CacheKey String keyWord,
                                @CacheKey String locId,
                                @CacheKey boolean loc,
                                @CacheKey boolean vs,
                                @CacheKey boolean hotel,
                                @CacheKey boolean restaurant,
                                @CacheKey boolean shopping,
                                @CacheKey int page,
                                @CacheKey int pageSize) {
        ObjectNode results = Json.newObject();
        try {

            Iterator<Locality> it;
            if (loc) {
                Locality locality;
                List<JsonNode> retLocList = new ArrayList<>();
                it = GeoAPI.searchLocalities(keyWord, true, null, page, pageSize);
                while (it.hasNext()) {
                    locality = it.next();
                    retLocList.add(new DetailedLocalityFormatter().format(locality));
                }
                results.put("locality", Json.toJson(retLocList));
            }

            List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
            if (vs)
                poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
            if (hotel)
                poiKeyList.add(PoiAPI.POIType.HOTEL);
            if (restaurant)
                poiKeyList.add(PoiAPI.POIType.RESTAURANT);
            if (shopping)
                poiKeyList.add(PoiAPI.POIType.SHOPPING);

            HashMap<PoiAPI.POIType, String> poiMap = new HashMap<PoiAPI.POIType, String>() {
                {
                    put(PoiAPI.POIType.VIEW_SPOT, "vs");
                    put(PoiAPI.POIType.HOTEL, "hotel");
                    put(PoiAPI.POIType.RESTAURANT, "restaurant");
                    put(PoiAPI.POIType.SHOPPING, "shopping");
                }
            };
            for (PoiAPI.POIType poiType : poiKeyList) {
                ObjectId oid = locId.equals("") ? null : new ObjectId(locId);
                // 发现POI
                List<JsonNode> retPoiList = new ArrayList<>();
                List<? extends AbstractPOI> itPoi = PoiAPI.poiSearchForTaozi(poiType, keyWord, oid, true, page, pageSize);
                for (AbstractPOI poi : itPoi)
                    retPoiList.add(new DetailedPOIFormatter<>(poi.getClass()).format(poi));
                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
            }
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }

    /**
     * 输入联想
     *
     * @param word
     * @param pageSize
     * @return
     */
    public static Result getSuggestions(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant, boolean shopping,
                                        int pageSize) {
        try {
            return Utils.createResponse(ErrorCode.NORMAL, getSuggestionsImpl(word, loc, vs, hotel, restaurant, shopping,
                    pageSize));
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    public static JsonNode getSuggestionsImpl(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant, boolean shopping,
                                              int pageSize) throws AizouException {
        ObjectNode ret = Json.newObject();

        List<JsonNode> locList = new ArrayList<>();
        if (loc) {
            for (Iterator<Locality> it = GeoAPI.searchLocalities(word, true, null, 0, pageSize); it.hasNext(); ) {
                Locality item = it.next();
                locList.add(new SuggestionFormatter().format(item));
            }
        }
        ret.put("locality", Json.toJson(locList.isEmpty() ? new ArrayList<>() : locList));


        List<JsonNode> vsList = new ArrayList<>();
        if (vs) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.VIEW_SPOT, word, pageSize);
                 it.hasNext(); )
                vsList.add(new SuggestionFormatter().format(it.next()));
        }
        ret.put("vs", Json.toJson(vsList.isEmpty() ? new ArrayList<>() : vsList));

        List<JsonNode> hotelList = new ArrayList<>();
        if (hotel) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.HOTEL, word, pageSize);
                 it.hasNext(); )
                hotelList.add(new SuggestionFormatter().format(it.next()));
        }

        ret.put("hotel", Json.toJson(hotelList.isEmpty() ? new ArrayList<>() : hotelList));

        List<JsonNode> restaurantList = new ArrayList<>();
        if (restaurant) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.RESTAURANT, word, pageSize);
                 it.hasNext(); )
                restaurantList.add(new SuggestionFormatter().format(it.next()));
        }

        ret.put("restaurant", Json.toJson(restaurantList.isEmpty() ? new ArrayList<>() : restaurantList));

        List<JsonNode> shoppingList = new ArrayList<>();
        if (shopping) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.SHOPPING, word, pageSize);
                 it.hasNext(); )
                shoppingList.add(new SuggestionFormatter().format(it.next()));
        }

        ret.put("shopping", Json.toJson(shoppingList.isEmpty() ? new ArrayList<>() : shoppingList));

        return ret;
    }

    /**
     * 取得图集
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getAlbums(String id, int page, int pageSize) {

        try {

            ObjectId oid = new ObjectId(id);
            // 默认情况会取出全部图集
            if (pageSize == 0)
                pageSize = Constants.MAX_COUNT;
            List<Images> items = MiscAPI.getLocalityAlbum(oid, page, pageSize);

            List<ObjectNode> nodeList = new ArrayList<>();
            for (Images images : items)
                nodeList.add((ObjectNode) new ImageFormatter().format(images));

            ObjectNode result = Json.newObject();
            result.put("album", Json.toJson(nodeList));
            result.put("albumCnt", nodeList.size());
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

//    public static Result testForTest() throws KrbException {
//        Configuration config = Configuration.root();
//        Configuration c = config.getConfig("solr");
//        return ok("ok");
//    }

//    public static Result explore(int details, int loc, int vs, int hotel, int restaurant, boolean abroad, int page, int pageSize) throws AizouException {
//        boolean detailsFlag = (details != 0);
//        ObjectNode results = Json.newObject();
//
//        // 发现城市
//        if (loc != 0) {
//            List<JsonNode> retLocList = new ArrayList<>();
//            for (Locality locality : LocalityAPI.explore(detailsFlag, abroad, page, pageSize))
//                retLocList.add(new DestinationFormatter().format(locality));
//            results.put("loc", Json.toJson(retLocList));
//        }
//
//        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
//        if (vs != 0)
//            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
//        if (hotel != 0)
//            poiKeyList.add(PoiAPI.POIType.HOTEL);
//        if (restaurant != 0)
//            poiKeyList.add(PoiAPI.POIType.RESTAURANT);
//
//        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<PoiAPI.POIType, String>() {
//            {
//                put(PoiAPI.POIType.VIEW_SPOT, "vs");
//                put(PoiAPI.POIType.HOTEL, "hotel");
//                put(PoiAPI.POIType.RESTAURANT, "restaurant");
//            }
//        };
//
//        for (PoiAPI.POIType poiType : poiKeyList) {
//            if (poiType == PoiAPI.POIType.VIEW_SPOT) {
//                results.put(poiMap.get(poiType), Json.toJson(new ArrayList<>()));
//            } else {
//                // 发现POI
//                List<JsonNode> retPoiList = new ArrayList<>();
//                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, (ObjectId) null, abroad, page, pageSize);
//                     it.hasNext(); )
//                    retPoiList.add(it.next().toJson(2));
//                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
//            }
//        }
//
//        return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
//    }
}
