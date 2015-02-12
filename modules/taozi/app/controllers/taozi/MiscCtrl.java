package controllers.taozi;

import aizou.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.aspectj.CatchException;
import controllers.aspectj.Key;
import controllers.aspectj.UsingOcsCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.LocalityFormatter;
import formatter.taozi.geo.SimpleLocalityFormatter;
import formatter.taozi.misc.ColumnFormatter;
import formatter.taozi.misc.CommentFormatter;
import formatter.taozi.misc.RecomFormatter;
import formatter.taozi.poi.BriefPOIFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import formatter.taozi.user.FavoriteFormatter;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.*;
import models.poi.AbstractPOI;
import models.poi.Comment;
import models.user.Favorite;
import models.user.UserInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.TaoziDataFilter;
import utils.Utils;

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
    public static String UPLOAD_URL_SMALL = "urlSmall";
    public static String UPLOAD_UID = "userId";

    /**
     * 封面故事,获取App首页的图像。
     *
     * @param width  指定宽度
     * @param height 指定高度
     * @return
     */
    @UsingOcsCache(key = "appHomeImage,{w},{h},{q},{fmt}", expireTime = 3600)
    public static F.Promise<Result> appHomeImage(@Key(tag = "w") final int width, @Key(tag = "h") final int height,
                                                  @Key(tag = "q") final int quality, @Key(tag = "fmt") final String format,
                                                  final int interlace)
            throws AizouException {

        F.Promise<F.Either<MiscInfo, Throwable>> promiseOfInfo = F.Promise.promise(
                new F.Function0<F.Either<MiscInfo, Throwable>>() {
                    @Override
                    public F.Either<MiscInfo, Throwable> apply() throws Throwable {
                        try {
                            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
                            return F.Either.Left(ds.createQuery(MiscInfo.class).field("key")
                                    .equal(MiscInfo.FD_TAOZI_COVERSTORY_IMAGE).get());
                        } catch (Throwable e) {
                            return F.Either.Right(e);
                        }
                    }
                }
        );

        return promiseOfInfo.map(new F.Function<F.Either<MiscInfo, Throwable>, Result>() {
            @Override
            @CatchException
            public Result apply(F.Either<MiscInfo, Throwable> miscInfoThrowableEither) throws Throwable {
                if (miscInfoThrowableEither.left != null && miscInfoThrowableEither.left.isDefined()) {
                    MiscInfo info = miscInfoThrowableEither.left.get();
                    ObjectNode node = Json.newObject();
                    // 示例：http://zephyre.qiniudn.com/misc/Kirkjufellsfoss_Sunset_Iceland5.jpg?imageView/1/w/400/h/200/q/85/format/webp/interlace/1
                    String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", info.value,
                            width, height, quality, format, interlace);

                    node.put("image", url);
                    node.put("width", width);
                    node.put("height", height);
                    node.put("fmt", format);
                    node.put("quality", quality);

                    return Utils.createResponse(ErrorCode.NORMAL, node);
                } else if (miscInfoThrowableEither.right != null && miscInfoThrowableEither.right.isDefined()) {
                    throw miscInfoThrowableEither.right.get();
                } else
                    throw new AizouException(ErrorCode.ILLEGAL_STATE);
            }
        });
    }

    public static Result postFeedback() throws AizouException {
        JsonNode feedback = request().body().asJson();

        Integer uid = request().hasHeader("UserId") ? Integer.parseInt(request().getHeader("UserId")) : null;
        String body = feedback.has("body") ? feedback.get("body").asText().trim() : null;
        if (body == null || body.equals(""))
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid feedback content.");
        Feedback feedBack = new Feedback();
        Datastore dsSave = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        if (uid != null)
            feedBack.uid = uid;
        feedBack.body = body;
        feedBack.time = new Date();
        feedBack.origin = Constants.APP_FLAG_TAOZI;
        feedBack.setEnabled(true);
        dsSave.save(feedBack);
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 获取推荐信息
     *
     * @return
     */
    @UsingOcsCache(key = "recommend", expireTime = 3600)
    public static Result recommend(int page, int pageSize)
            throws JsonProcessingException, ReflectiveOperationException, AizouException {

        List<ObjectNode> retNodeList = new ArrayList<>();
        Datastore ds;

        ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Recom> query = ds.createQuery(Recom.class);

        query.field("enabled").equal(Boolean.TRUE);
        query.order("weight").offset(page * pageSize).limit(pageSize);
        Recom recom;
        Map<String, List<Recom>> map = new LinkedHashMap<>();
        List<Recom> tempList;

        for (Recom aQuery : query) {
            recom = aQuery;
            tempList = map.get(recom.type);
            if (tempList == null)
                tempList = new ArrayList<>();
            tempList.add(recom);
            map.put(recom.type, tempList);
        }

        String key;
        ObjectNode tempNode;
        List<Recom> recList;
        RecomFormatter recomFormatter = FormatterFactory.getInstance(RecomFormatter.class);
        for (Map.Entry<String, List<Recom>> entry : map.entrySet()) {
            key = entry.getKey();
            recList = entry.getValue();
            tempNode = Json.newObject();
            tempNode.put("title", key == null ? "" : key);
            tempNode.put("contents", recomFormatter.formatNode(recList));
            retNodeList.add(tempNode);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(retNodeList));
    }

    /**
     * 添加收藏
     *
     * @return
     */
    public static Result addFavorite() throws AizouException {
        JsonNode collection = request().body().asJson();

        Integer userId = Integer.parseInt(request().getHeader("UserId"));
        String itemId = collection.get("itemId").asText();
        ObjectId oid = new ObjectId(itemId);
        String type = collection.get("type").asText();
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Favorite> query = ds.createQuery(Favorite.class);
        query.field("userId").equal(userId).field("type").equal(type).field("itemId").equal(oid);
        if (query.iterator().hasNext()) {
            // 如果已收藏，则更新收藏时间，并返回已收藏的提示
            UpdateOperations<Favorite> update = ds.createUpdateOperations(Favorite.class);
            update.set(Favorite.fnCreateTime, new Date());
            ds.update(query, update);
            return Utils.createResponse(ErrorCode.DATA_EXIST, "Favorite item has existed");
        }
        Favorite fa = new Favorite();
        fa.setId(new ObjectId());
        fa.itemId = oid;
        fa.type = type;
        fa.userId = userId;
        fa.createTime = new Date();
        ds.save(fa);

        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得收藏
     *
     * @return
     */
    public static Result getFavorite(String faType, int page, int pageSize)
            throws JsonProcessingException, AizouException {

        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        Integer userId = Integer.parseInt(request().getHeader("UserId"));
        Query<Favorite> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).createQuery(Favorite.class);
        query.field("userId").equal(userId);

        if (faType != null && !faType.equals("all") && !faType.equals(""))
            query.field(Favorite.fnType).equal(faType);

        query.offset(page * pageSize).limit(pageSize);
        query.order(String.format("-%s", Favorite.fnCreateTime));

        List<Favorite> faList = query.asList();
        if (faList == null || faList.isEmpty())
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(new ArrayList<Favorite>()));
        List<Favorite> faShowList = new ArrayList<>();
        List<Favorite> noteFav = new ArrayList<>();
        List<ObjectId> travelNoteIds = new ArrayList<>();
        String type;
        Locality loc;
        AbstractPOI poi;
        PoiAPI.POIType poiType;
        List<String> locFields = new ArrayList<>();
        Collections.addAll(locFields, "id", "zhName", "enName", "images", "desc", "timeCostDesc");
        List<String> poiFields = new ArrayList<>();
        Collections.addAll(poiFields, "id", "zhName", "enName", "images", "desc", "type", "locality", "address", "price", "timeCostDesc", "rating");
        for (Favorite fa : faList) {
            type = fa.type;
            switch (type) {
                case "locality":
                    loc = GeoAPI.locDetails(fa.itemId, locFields);
                    if (loc == null)
                        continue;
                    fa.zhName = loc.getZhName();
                    fa.enName = loc.getEnName();
                    fa.images = TaoziDataFilter.getOneImage(loc.getImages());
                    fa.desc = loc.getDesc();
                    // 城市显示建议游玩时间
                    fa.timeCostDesc = loc.getTimeCostDesc();
                    break;
                case "travelNote":
                    travelNoteIds.add(fa.itemId);
                    noteFav.add(fa);
                    continue;
                case "vs":
                case "hotel":
                case "restaurant":
                case "shopping":
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
                    fa.images = TaoziDataFilter.getOneImage(poi.images);
                    fa.desc = poi.desc;
                    fa.locality = poi.getLocality();
                    fa.timeCostDesc = poi.timeCostDesc;
                    fa.priceDesc = TaoziDataFilter.getPriceDesc(poi);
                    fa.rating = poi.rating;
                    fa.address = poi.address;
                    fa.telephone = poi.telephone;
                    break;
            }
            faShowList.add(fa);
        }
        List<String> noteFields = new ArrayList<>();
        Collections.addAll(locFields, "id", "zhName", "enName", "images", "desc");
        List<TravelNote> travelNotes = TravelNoteAPI.getNotesByIdList(travelNoteIds, noteFields);
        Map<String, TravelNote> travelNoteMap = new HashMap<>();
        for (TravelNote temp : travelNotes)
            travelNoteMap.put(temp.getId().toString(), temp);

        TravelNote tnFromFavorate;
        if (travelNotes.size() > 0) {
            for (Favorite fa : noteFav) {
                tnFromFavorate = travelNoteMap.get(fa.itemId.toString());
                if (tnFromFavorate == null)
                    continue;
                fa.zhName = tnFromFavorate.getName();
                fa.enName = tnFromFavorate.getName();
                ImageItem tmg = new ImageItem();
                tmg.setKey(tnFromFavorate.getCover());
                fa.images = Arrays.asList(tmg);
                fa.desc = tnFromFavorate.getDesc();
                faShowList.add(fa);
            }
        }
        // 按照创建时间排序
        Collections.sort(faShowList, new Comparator<Favorite>() {
            public int compare(Favorite arg0, Favorite arg1) {
                return arg0.createTime.getTime() - arg1.createTime.getTime() > 0 ? -1 : 1;
            }
        });
        FavoriteFormatter favoriteFormatter = FormatterFactory.getInstance(FavoriteFormatter.class, imgWidth);

//            for (Favorite fa : faShowList)
//                nodes.add((ObjectNode) new SelfFavoriteFormatterOld(fa.type).format(fa));
        return Utils.createResponse(ErrorCode.NORMAL, favoriteFormatter.formatNode(faShowList));

    }

    /**
     * 删除收藏
     *
     * @return
     */
    public static Result delFavorite(String id) throws AizouException {
        ObjectId oid = new ObjectId(id);
        Integer userId = Integer.parseInt(request().getHeader("UserId"));
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Favorite> query = ds.createQuery(Favorite.class).field("userId").equal(userId).field("itemId").equal(oid);
        ds.delete(query);

        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

//    /**
//     * 通过城市id获得天气情况
//     *
//     * @param id
//     * @return
//     * @throws exception.AizouException
//     */
//    public static Result getWeatherDetail(String id) {
//        try {
//            YahooWeather weather = WeatherAPI.weatherDetails(new ObjectId(id));
//            return Utils.createResponse(ErrorCode.NORMAL, new WeatherFormatter().format(weather));
//        } catch (NullPointerException | AizouException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
//        }
//    }

    /**
     * 获得资源上传凭证
     *
     * @param scenario 上传场景: PORTRAIT-上传头像
     * @return
     */
    public static Result putPolicy(String scenario) throws InvalidKeyException, NoSuchAlgorithmException {
        Configuration config = Configuration.root();

        String userId = request().getHeader("UserId");
        String picName = getPicName(Integer.parseInt(request().getHeader("UserId")));
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

        ObjectNode ret = Json.newObject();
        ret.put("uploadToken", accessKey + ":" + encodedSign + ":" + encodedPutPolicy);
        ret.put("key", picName);
        return Utils.createResponse(ErrorCode.NORMAL, ret);
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
        StringBuilder callbackBody = new StringBuilder(10);
        callbackBody.append("name=$(fname)");
        callbackBody.append("&size=$(fsize)");
        callbackBody.append("&h=$(imageInfo.height)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&hash=$(etag)");
        callbackBody.append("&bucket=$(bucket)");
        String url = "http://" + "$(bucket)" + ".qiniudn.com" + Constants.SYMBOL_SLASH + "$(key)";
        // 定义图片的URL
        callbackBody.append("&").append(UPLOAD_URL).append("=").append(url);
        callbackBody.append("&").append(UPLOAD_URL_SMALL).append("=").append(url).append("?imageView2/2/w/200");
        // 定义用户ID
        callbackBody.append("&").append(UPLOAD_UID).append("=").append(userId);
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
    public static String getPicName(long userId) {
        Date date = new Date();
        return String.format("avt_%d_%d.jpg", userId, date.getTime());
    }

    /**
     * 上传回调
     *
     * @return
     */
    public static Result getCallback() throws AizouException {
        Map<String, String[]> fav = request().body().asFormUrlEncoded();
        ObjectNode ret = Json.newObject();
        String url = null;
//        String urlSmall = null;
        String userId = null;
        for (Map.Entry<String, String[]> entry : fav.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            //LogUtils.info(MiscCtrl.class, key + "&&" + value[0]);
            if (key.equals(UPLOAD_URL))
                url = value[0];
            if (key.equals(UPLOAD_UID))
                userId = value[0];
//            if (key.equals(UPLOAD_URL_SMALL))
//                urlSmall = value[0];
            ret.put(key, value[0]);
//            LogUtils.info(MiscCtrl.class, key + "&&" + value[0]);
        }
        ret.put("success", true);

        // TODO userId在什么情况下可能为null？
        if (userId != null)
            UserAPI.resetAvater(Integer.valueOf(userId), url);

        return ok(ret);
    }
//
//    private static String delSpe(String str){
//        return str.replaceAll("\\\\", "");
//    }

    /**
     * 旅行专栏
     *
     * @return
     */
    @UsingOcsCache(key = "getColumns({type},{id})", expireTime = 86400)
    public static Result getColumns(@Key(tag = "type") String type, @Key(tag = "id") String id)
            throws AizouException {

        String url;
        Configuration config = Configuration.root();
        Map h5 = (Map) config.getObject("h5");
        url = String.format("http://%s%s?id=", h5.get("host").toString(), h5.get("column").toString());

        List<Column> columnList = MiscAPI.getColumns(type, id);
        for (Column c : columnList) {
            c.setLink(url + c.getId().toString());
        }
        ColumnFormatter columnFormatter = FormatterFactory.getInstance(ColumnFormatter.class);

        return Utils.createResponse(ErrorCode.NORMAL, columnFormatter.formatNode(columnList));
    }

    /**
     * 保存用户的评论
     *
     * @return
     */
    public static Result saveComment(String poiId)
            throws AizouException {
        Comment comment = new Comment();

        JsonNode req = request().body().asJson();
        String userId = request().getHeader("UserId");
        Long userIdLong = Long.parseLong(userId);
        UserInfo user = UserAPI.getUserInfo(userIdLong,
                Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar));
        if (user != null) {
            comment.setAuthorName(user.getNickName());
            comment.setAuthorAvatar(user.getAvatar());
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
        comment.setPublishTime(commentTime);
        comment.setmTime(commentTime);

        JsonNode result = MiscAPI.saveComment(comment);
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    private static JsonNode getCommentsImpl(String poiId, double lower, double upper, long lastUpdate, int pageSize)
            throws AizouException {
        CommentFormatter formatter = FormatterFactory.getInstance(CommentFormatter.class);
        List<Comment> commentList = MiscAPI.displayCommentApi(new ObjectId(poiId), lower, upper, lastUpdate, 0, pageSize);

        return formatter.formatNode(commentList);
    }

    /**
     * 显示评论信息
     *
     * @param poiId
     * @param lastUpdate
     * @param pageSize
     * @return
     */
    public static Result displayComment(String poiId, double lower, double upper, long lastUpdate, int pageSize)
            throws AizouException {
        JsonNode results = getCommentsImpl(poiId, lower, upper, lastUpdate, pageSize);
        return Utils.createResponse(ErrorCode.NORMAL, results);
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
    @UsingOcsCache(key = "search(keyWord={keyWord},locId={locId},loc={loc},vs={vs},hotel={hotel}," +
            "restaurant={restaurant},shopping={shopping},page={p},pageSize={ps})",
            expireTime = 300)
    public static Result search(@Key(tag = "keyWord") String keyWord,
                                @Key(tag = "locId") String locId,
                                @Key(tag = "loc") boolean loc,
                                @Key(tag = "vs") boolean vs,
                                @Key(tag = "hotel") boolean hotel,
                                @Key(tag = "restaurant") boolean restaurant,
                                @Key(tag = "shopping") boolean shopping,
                                @Key(tag = "p") int page,
                                @Key(tag = "ps") int pageSize)
            throws SolrServerException, AizouException {
        ObjectNode results = Json.newObject();

        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        Iterator<Locality> it;
        if (loc) {

            Locality locality;
            List<Locality> retLocList = new ArrayList<>();

            LocalityFormatter localityFormatter = FormatterFactory.getInstance(LocalityFormatter.class, imgWidth);
            it = GeoAPI.searchLocalities(keyWord, true, null, page, pageSize,
                    localityFormatter.getFilteredFields());
            while (it.hasNext()) {
                locality = it.next();
                retLocList.add(locality);
            }

            results.put("locality", localityFormatter.formatNode(retLocList));
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

        SimplePOIFormatter<? extends AbstractPOI> poiFormatter;
        for (PoiAPI.POIType poiType : poiKeyList) {
            ObjectId oid = locId.equals("") ? null : new ObjectId(locId);
            // 发现POI
            List<JsonNode> retPoiList = new ArrayList<>();
            List<? extends AbstractPOI> itPoi = PoiAPI.poiSearchForTaozi(poiType, keyWord, oid, true, page, pageSize);
            for (AbstractPOI poi : itPoi) {
                poi.images = TaoziDataFilter.getOneImage(poi.images);
                poi.desc = StringUtils.abbreviate(poi.desc, Constants.ABBREVIATE_LEN);
                poiFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
                retPoiList.add(poiFormatter.formatNode(poi));
            }
            results.put(poiMap.get(poiType), Json.toJson(retPoiList));
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
    public static Result getSuggestions(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                        boolean shopping, int pageSize)
            throws AizouException {
        return Utils.createResponse(ErrorCode.NORMAL, getSuggestionsImpl(word, loc, vs, hotel, restaurant, shopping,
                pageSize));
    }

    public static JsonNode getSuggestionsImpl(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                              boolean shopping, int pageSize)
            throws AizouException {
        ObjectNode ret = Json.newObject();

        List<Locality> locList = new ArrayList<>();
        JsonNode node = Json.newObject();
        if (loc) {
            for (Iterator<Locality> it = GeoAPI.searchLocalities(word, true, null, 0, pageSize); it.hasNext(); ) {
                locList.add(it.next());
            }
            SimpleLocalityFormatter simpleLocalityFormatter = FormatterFactory.getInstance(SimpleLocalityFormatter.class);
            node = simpleLocalityFormatter.formatNode(locList);
        }
        ret.put("locality", locList.isEmpty() ? Json.toJson(new ArrayList<>()) : node);

        BriefPOIFormatter briefPOIFormatter = FormatterFactory.getInstance(BriefPOIFormatter.class);
        List<AbstractPOI> vsList = new ArrayList<>();
        if (vs) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.VIEW_SPOT, word, pageSize);
                 it.hasNext(); )
                vsList.add(it.next());
        }
        ret.put("vs", vsList.isEmpty() ? Json.toJson(new ArrayList<>()) : briefPOIFormatter.formatNode(vsList));

        List<AbstractPOI> hotelList = new ArrayList<>();
        if (hotel) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.HOTEL, word, pageSize);
                 it.hasNext(); )
                hotelList.add(it.next());
        }
        ret.put("hotel", hotelList.isEmpty() ? Json.toJson(new ArrayList<>()) : briefPOIFormatter.formatNode(hotelList));

        List<AbstractPOI> restaurantList = new ArrayList<>();
        if (restaurant) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.RESTAURANT, word, pageSize);
                 it.hasNext(); )
                restaurantList.add(it.next());
        }
        ret.put("restaurant", restaurantList.isEmpty() ? Json.toJson(new ArrayList<>()) : briefPOIFormatter.formatNode(restaurantList));

        List<AbstractPOI> shoppingList = new ArrayList<>();
        if (shopping) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.SHOPPING, word, pageSize);
                 it.hasNext(); )
                shoppingList.add(it.next());
        }
        ret.put("shopping", shoppingList.isEmpty() ? Json.toJson(new ArrayList<>()) : briefPOIFormatter.formatNode(shoppingList));

        return ret;
    }

    /**
     * 取得图集
     * 默认情况会取出全部图集
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getAlbums(String id, int page, int pageSize) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 200;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        ObjectId oid = new ObjectId(id);
        List<Images> items = MiscAPI.getLocalityAlbum(oid, page, pageSize);
        Long amount = MiscAPI.getLocalityAlbumCount(oid);

        ObjectNode imgNode;
        List<ObjectNode> nodeList = new ArrayList<>();
        for (Images images : items) {
            imgNode = Json.newObject();
            imgNode.put("url", String.format("%s?imageView2/2/w/%d", images.getFullUrl(), imgWidth));
            imgNode.put("originUrl", String.format("%s?imageView2/2/w/%d", images.getFullUrl(), 960));
            nodeList.add(imgNode);
        }
        ObjectNode result = Json.newObject();
        result.put("album", Json.toJson(nodeList));
        result.put("albumCnt", amount);
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    /**
     * 获得更新信息
     *
     * @return
     */
    public static Result getUpdates() throws AizouException {
        String platform = request().getHeader("Platform");
        String ver = request().getHeader("Version");
        if (ver == null || ver.isEmpty() || platform == null || platform.isEmpty())
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID ARGUMENT");
        platform = platform.toLowerCase();
        ver = ver.toLowerCase();

        double oldVerN = 0;
        String[] oldVerP = ver.split("\\.");
        for (int i = 0; i < oldVerP.length; i++)
            oldVerN += Math.pow(10, -3 * i) * Double.parseDouble(oldVerP[i]);

        List<String> keyList = new ArrayList<>();
        Collections.addAll(keyList, MiscInfo.FD_UPDATE_ANDROID_VERSION, MiscInfo.FD_UPDATE_ANDROID_URL);
        Map<String, String> miscInfos;

        miscInfos = MiscAPI.getMiscInfos(keyList);

        String newVerS = miscInfos.get(MiscInfo.FD_UPDATE_ANDROID_VERSION);
        String[] newVerP = newVerS.split("\\.");
        double newVerN = 0;
        for (int i = 0; i < newVerP.length; i++)
            newVerN += Math.pow(10, -3 * i) * Double.parseDouble(newVerP[i]);

        ObjectNode result = Json.newObject();
        if (newVerN > oldVerN) {
            result.put("update", true);
            result.put("version", newVerS);
            result.put("downloadUrl", miscInfos.get(MiscInfo.FD_UPDATE_ANDROID_URL));
        } else
            result.put("update", false);
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }
}
