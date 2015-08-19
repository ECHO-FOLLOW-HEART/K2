package controllers.app;

import aizou.core.*;
import aspectj.Key;
import aspectj.UsingOcsCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.LocalityFormatter;
import formatter.taozi.geo.SimpleLocalityFormatter;
import formatter.taozi.misc.ColumnFormatter;
import formatter.taozi.misc.CommentFormatter;
import formatter.taozi.misc.RecomFormatter;
import formatter.taozi.misc.SimpleRefFormatter;
import formatter.taozi.poi.BriefPOIFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import formatter.taozi.user.FavoriteFormatter;
import database.MorphiaFactory;
import misc.FinagleUtils$;
import models.geo.Locality;
import models.misc.*;
import models.poi.AbstractPOI;
import models.poi.Comment;
import models.poi.RestaurantComment;
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
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.LogUtils;
import utils.TaoziDataFilter;
import utils.Utils;
import utils.results.SceneID;
import utils.results.TaoziResBuilder;
import utils.results.TaoziSceneText;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    public static String UPLOAD_SCENARIO = "scenario";
    private Map qiniu;

    /**
     * 封面故事,获取App首页的图像。
     *
     * @param width  指定宽度
     * @param height 指定高度
     * @return
     */
    @UsingOcsCache(key = "appHomeImage|{w}|{h}|{q}|{fmt}", expireTime = 86400)
    public static Result appHomeImage(@Key(tag = "w") int width, @Key(tag = "h") int height,
                                      @Key(tag = "q") int quality, @Key(tag = "fmt") String format, int interlace)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        List<MiscInfo> infos = ds.createQuery(MiscInfo.class).field("key").equal(MiscInfo.FD_TAOZI_COVERSTORY_IMAGE)
                .asList();
        if (infos == null)
            return new TaoziResBuilder().setCode(ErrorCode.UNKOWN_ERROR)
                    .setMessage(TaoziSceneText.instance().text(SceneID.ERR_APP_HOME_IMAGE))
                    .build();

        // 示例：http://zephyre.qiniudn.com/misc/Kirkjufellsfoss_Sunset_Iceland5.jpg?imageView/1/w/400/h/200/q/85/format/webp/interlace/1
        //String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", info.value,width, height, quality, format, interlace);
        //double appRatio = (Math.round(height / width)*100 / 100.0);
        DecimalFormat df = new DecimalFormat("###.0000");
        BigDecimal b1 = new BigDecimal(df.format(height));
        BigDecimal b2 = new BigDecimal(df.format(width));
        // 取得app屏幕的高宽比
        double appRatio = b1.divide(b2, 4).doubleValue();
        double ratio;
        // 初始值取一个较大的数
        double suitDif = 10;
        double dif;
        String suitImg = "";
        // 取数据库中最接近app高宽比的图片
        for (MiscInfo info : infos) {
            ratio = Double.valueOf(info.viceKey);
            dif = Math.abs(appRatio - ratio);
            if (dif < suitDif) {
                suitDif = dif;
                suitImg = info.value;
            }
        }
        String url = String.format("%s?imageView/1/w/%d/h/%d/q/%d/format/%s/interlace/%d", suitImg, width, height, quality, format, interlace);
        ObjectNode node = Json.newObject();
        node.put("image", url);
        node.put("width", width);
        node.put("height", height);
        node.put("fmt", format);
        node.put("quality", quality);

        return new TaoziResBuilder().setBody(node).build();
    }

    public static Result postFeedback() throws AizouException {
        JsonNode feedback = request().body().asJson();

        Integer uid = request().hasHeader("UserId") ? Integer.parseInt(request().getHeader("UserId")) : null;
        String body = feedback.has("body") ? feedback.get("body").asText().trim() : null;
        if (body == null || body.equals(""))
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid feedback content.");
        Feedback feedBack = new Feedback();
        Datastore dsSave = MorphiaFactory.datastore();
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
    @UsingOcsCache(key = "recommend|{page}|{pageSize}", expireTime = 3600)
    public static Result recommend(@Key(tag = "page") int page, @Key(tag = "pageSize") int pageSize)
            throws JsonProcessingException, ReflectiveOperationException, AizouException {

        List<ObjectNode> retNodeList = new ArrayList<>();
        Datastore ds;

        ds = MorphiaFactory.datastore();
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

        return new TaoziResBuilder().setBody(Json.toJson(retNodeList)).build();
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
        Datastore ds = MorphiaFactory.datastore();
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
        Query<Favorite> query = MorphiaFactory.datastore().createQuery(Favorite.class);
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
        Datastore ds = MorphiaFactory.datastore();
        Query<Favorite> query = ds.createQuery(Favorite.class).field("userId").equal(userId).field("itemId").equal(oid);
        ds.delete(query);

        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 获得资源上传凭证
     *
     * @param scenario 上传场景: PORTRAIT-上传头像
     * @return
     */
    //@CheckUser
    public static Result putPolicy(String scenario) throws InvalidKeyException, NoSuchAlgorithmException, AizouException {
        Configuration config = Configuration.root();

        String userId = request().getHeader("UserId");
        String picName = getPicName(Integer.parseInt(request().getHeader("UserId")));
        Map qiniu = (Map) config.getObject("qiniu");
        String secretKey = qiniu.get("secertKey").toString();
        String accessKey = qiniu.get("accessKey").toString();
        String scope, callbackUrl;
        if (scenario.equals("portrait") || scenario.equals("album")) {
            scope = qiniu.get("taoziAvaterScope").toString();
            String hostname = "api.lvxingpai.com";
            String url = routes.MiscCtrl.getCallback().url();
            callbackUrl = new StringBuilder().append("http://").append(hostname).append(url).toString();
            LogUtils.info(MiscCtrl.class, "Test Upload CallBack.callbackUrl:" + callbackUrl);
        } else
            return new TaoziResBuilder().setCode(ErrorCode.INVALID_ARGUMENT)
                    .setMessage(TaoziSceneText.instance().text(SceneID.INVALID_UPLOAD_SCENE))
                    .build();

        //取得上传策略
        ObjectNode policy = getPutPolicyInfo(scope, picName, callbackUrl, Integer.valueOf(userId), scenario);
        // UrlBase64编码
        String encodedPutPolicy = Base64.encodeBase64URLSafeString(policy.toString().trim().getBytes());
        encodedPutPolicy = Utils.base64Padding(encodedPutPolicy);
        // 构造密钥并UrlBase64编码
        String encodedSign = Base64.encodeBase64URLSafeString(Utils.hmac_sha1(encodedPutPolicy, secretKey));
        encodedSign = Utils.base64Padding(encodedSign);

        ObjectNode ret = Json.newObject();
        ret.put("uploadToken", accessKey + ":" + encodedSign + ":" + encodedPutPolicy);
        ret.put("key", picName);

        return new TaoziResBuilder().setBody(ret).build();
    }

    /**
     * 回调方式，上传用户头像，上传策略序列化为JSON
     *
     * @return
     */
    private static ObjectNode getPutPolicyInfo(String scope, String picName, String callbackUrl, Integer userId, String scenario) {

        ObjectNode info = Json.newObject();
        info.put("scope", scope + ":" + picName);
        info.put("deadline", System.currentTimeMillis() / 1000 + 2 * 3600);
        info.put("callBackBody", getCallBackBody(userId, scenario));
        info.put("callbackUrl", callbackUrl);
        return info;
    }

    /**
     * 定义回调Body
     *
     * @return
     */
    private static String getCallBackBody(Integer userId, String scenario) {
        String picId = new ObjectId().toString();
        StringBuilder callbackBody = new StringBuilder(10);
        callbackBody.append("name=$(fname)");
        callbackBody.append("&size=$(fsize)");
        callbackBody.append("&h=$(imageInfo.height)");
        callbackBody.append("&w=$(imageInfo.width)");
        callbackBody.append("&hash=$(etag)");
        callbackBody.append("&bucket=$(bucket)");
        callbackBody.append("&key=$(key)");
        callbackBody.append("&id=" + picId);
        LogUtils.info(MiscCtrl.class, "Magic Id:" + picId);
        String url = "http://" + "$(bucket)" + ".qiniudn.com" + Constants.SYMBOL_SLASH + "$(key)";
        // 定义图片的URL
        callbackBody.append("&").append(UPLOAD_URL).append("=").append(url);
        callbackBody.append("&").append(UPLOAD_URL_SMALL).append("=").append(url).append("?imageView2/2/w/200");
        // 定义用户ID
        callbackBody.append("&").append(UPLOAD_UID).append("=").append(userId);
        callbackBody.append("&").append(UPLOAD_SCENARIO).append("=").append(scenario);
        return callbackBody.toString();
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
        String scenario = null;
        String url = null;
//        String urlSmall = null;
        String userId = null;
        String id = null;
        for (Map.Entry<String, String[]> entry : fav.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (key.equals(UPLOAD_URL))
                url = value[0];
            if (key.equals(UPLOAD_UID))
                userId = value[0];
            if (key.equals("id"))
                id = value[0];
            if (key.equals(UPLOAD_SCENARIO)) {
                scenario = value[0];
                // LogUtils.info(MiscCtrl.class, "Test Upload CallBack.Scenario:" + scenario, key + "&&" + value[0]);
            }
            ret.put(key, value[0]);
        }

        if (scenario != null && scenario.equals("album")) {
            LogUtils.info(MiscCtrl.class, "Test scenario.equals(\"album\"):" + scenario);
            ImageItem imageItem = getImageFromCallBack(ret);
            if (imageItem == null)
                return status(500, "Can't get image key!");
            UserUgcAPI.addUserAlbum(Long.valueOf(userId), imageItem, id);
        } else {
            LogUtils.info(MiscCtrl.class, "Start." + userId.toString() + url);
            FinagleUtils$.MODULE$.updateUserAvatar(userId, url);
            LogUtils.info(MiscCtrl.class, "End." + userId.toString() + url);
        }
        //UserAPI.resetAvater(Long.valueOf(userId), url);
        ret.put("success", true);

        return ok(ret);
    }

    private static ImageItem getImageFromCallBack(ObjectNode ret) {
        ImageItem imageItem = new ImageItem();
        // 如果没有url,返回空对象
        if (ret.get("key") == null)
            return null;
        imageItem.setKey(ret.get("key").asText());

        if (ret.get("w") != null && ret.get("w").canConvertToInt())
            imageItem.setW(ret.get("w").asInt());
        if (ret.get("h") != null && ret.get("h").canConvertToInt())
            imageItem.setH(ret.get("h").asInt());
        if (ret.get("size") != null && ret.get("size").canConvertToInt())
            imageItem.setSize(ret.get("size").asInt());
        if (ret.get("bucket") != null)
            imageItem.setBucket(ret.get("bucket").asText());
        return imageItem;
    }

    /**
     * 旅行专栏
     *
     * @return
     */
    @UsingOcsCache(key = "getColumns|{type}|{id}", expireTime = 86400, serializer = "WrappedResult")
    public static Result getColumns(@Key(tag = "type") String type, @Key(tag = "id") String id)
            throws AizouException {

        Configuration config = Configuration.root();
        Map h5 = (Map) config.getObject("h5");
        String url = String.format("http://%s%s?id=", h5.get("host").toString(), h5.get("column").toString());

        List<Column> columnList = MiscAPI.getColumns(type, id);
        for (Column c : columnList) {
            if (c.getLinkType() != null && c.getLinkType().equals(Column.FD_LINKTYPE_JOIN))
                c.setLink(url + c.getId().toString());
        }
        ColumnFormatter columnFormatter = FormatterFactory.getInstance(ColumnFormatter.class);

        return new TaoziResBuilder().setBody(columnFormatter.formatNode(columnList)).build();
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

    public static JsonNode getCommentsImpl(Class<? extends Comment> commitClass, String poiId, double lower, double upper, long lastUpdate, int page, int pageSize)
            throws AizouException {
        CommentFormatter formatter = FormatterFactory.getInstance(CommentFormatter.class);
        List<Comment> commentList = MiscAPI.getComments(commitClass, new ObjectId(poiId), lower, upper, lastUpdate, page, pageSize);

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
    public static Result displayComment(String poiType, String poiId, double lower, double upper, long lastUpdate, int page, int pageSize)
            throws AizouException {

        Class<? extends Comment> commentClass = Comment.class;
        switch (poiType) {
            case "vs":
                commentClass = Comment.class;
                break;
            case "hotel":
                //commentClass = HotelComment.class;
                break;
            case "restaurant":
                commentClass = RestaurantComment.class;
                break;
            case "shopping":
                //commentClass = ShoppingComment.class;
                break;
            default:
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));
        }

        JsonNode results = getCommentsImpl(commentClass, poiId, lower, upper, lastUpdate, page, pageSize);
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
    @UsingOcsCache(key = "search|{keyWord}|{locId}|{loc}|{vs}|{hotel}|" +
            "{restaurant}|{shopping}|{p}|{ps})",
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

        SimplePOIFormatter<? extends AbstractPOI> poiFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
        for (PoiAPI.POIType poiType : poiKeyList) {
            ObjectId oid = locId.equals("") ? null : new ObjectId(locId);
            // 发现POI
            List<AbstractPOI> retPoiList = new ArrayList<>();
            List<? extends AbstractPOI> itPoi = PoiAPI.poiSearchForTaozi(poiType, keyWord, oid, true, page, pageSize);
            for (AbstractPOI poi : itPoi) {
                poi.images = TaoziDataFilter.getOneImage(poi.images);
                poi.desc = StringUtils.abbreviate(poi.desc, Constants.ABBREVIATE_LEN);
                //poiFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
                retPoiList.add(poi);
            }
            results.put(poiMap.get(poiType), poiFormatter.formatNode(retPoiList));
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

    public static Result getLocalityComments(String commentType, String id, int page, int pageSize) {
        Result result = null;
        return result;
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

    /**
     * 举报
     *
     * @return
     * @throws AizouException
     */
    public static Result postTipOff() throws AizouException {
        JsonNode node = request().body().asJson();

        Long selfId = Long.parseLong(request().getHeader("UserId"));
        //Long offerUserId = Long.parseLong(node.get("offerUserId").asText());
        Long targetUserId = Long.parseLong(node.get("targetUserId").asText());
        String body = node.get("body").asText().trim();
        if (body == null || body.equals(""))
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid tipOff content.");
        TipOff tipOff = new TipOff();
        Datastore dsSave = MorphiaFactory.datastore();

        tipOff.setOfferUserId(selfId);
        tipOff.setTargetUserId(targetUserId);
        tipOff.setBody(body);
        tipOff.setcTime(System.currentTimeMillis());
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        tipOff.setDate(fmt.format(tipOff.getcTime()));
        tipOff.setTaoziEna(true);
        dsSave.save(tipOff);
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 取得热门搜索
     *
     * @return
     * @throws AizouException
     */
    public static Result getHotSearchs() throws AizouException {
        List<SimpleRef> simpleRefs = new ArrayList<>();
        SimpleRef simpleRef1 = new SimpleRef();
        simpleRef1.setId(new ObjectId("5473ccd7b8ce043a64108c46"));
        simpleRef1.setZhName("北京");
        simpleRefs.add(simpleRef1);

        SimpleRef simpleRef2 = new SimpleRef();
        simpleRef2.setId(new ObjectId("5473ccdfb8ce043a64108d97"));
        simpleRef2.setZhName("厦门");
        simpleRefs.add(simpleRef2);

        SimpleRef simpleRef3 = new SimpleRef();
        simpleRef3.setId(new ObjectId("54ae73f85c142faec2f8e9e1"));
        simpleRef3.setZhName("盘飧市");
        simpleRefs.add(simpleRef3);

        SimpleRef simpleRef4 = new SimpleRef();
        simpleRef4.setId(new ObjectId("54ae6b5e5c142faec2f78262"));
        simpleRef4.setZhName("护国寺小吃店");
        simpleRefs.add(simpleRef4);

        SimpleRef simpleRef5 = new SimpleRef();
        simpleRef5.setId(new ObjectId("547bfde9b8ce043eb2d84c3a"));
        simpleRef5.setZhName("曾厝垵");
        simpleRefs.add(simpleRef5);

        SimpleRef simpleRef6 = new SimpleRef();
        simpleRef6.setId(new ObjectId("547bfe30b8ce043eb2d890ff"));
        simpleRef6.setZhName("西湖");
        simpleRefs.add(simpleRef6);

        SimpleRef simpleRef7 = new SimpleRef();
        simpleRef7.setId(new ObjectId("547bfdcab8ce043eb2d82cda"));
        simpleRef7.setZhName("拙政园");
        simpleRefs.add(simpleRef7);

        SimpleRef simpleRef8 = new SimpleRef();
        simpleRef8.setId(new ObjectId("547bfde7b8ce043eb2d84a44"));
        simpleRef8.setZhName("东方明珠");
        simpleRefs.add(simpleRef8);

        SimpleRef simpleRef9 = new SimpleRef();
        simpleRef9.setId(new ObjectId("54ae98c05c142faec2fcd939"));
        simpleRef9.setZhName("东门商业街");
        simpleRefs.add(simpleRef9);

        SimpleRef simpleRef10 = new SimpleRef();
        simpleRef10.setId(new ObjectId("54ae93335c142faec2fba606"));
        simpleRef10.setZhName("银泰中心");
        simpleRefs.add(simpleRef10);
        SimpleRefFormatter simpleRefFormatter = FormatterFactory.getInstance(SimpleRefFormatter.class);

        return Utils.createResponse(ErrorCode.NORMAL, simpleRefFormatter.formatNode(simpleRefs));
    }

}
