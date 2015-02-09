package controllers.taozi;

import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.CacheKey;
import controllers.UsingCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.poi.CommentFormatter;
import formatter.taozi.poi.DetailedPOIFormatter;
import formatter.taozi.poi.POIRmdFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import formatter.taozi.user.ContactFormatter;
import models.geo.Locality;
import models.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.TaoziDataFilter;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/11/1.
 */
public class POICtrl extends Controller {

    public static JsonNode viewPOIInfoImpl(Class<? extends AbstractPOI> poiClass, String spotId,
                                           int commentPage, int commentPageSize, Long userId,
                                           int rmdPage, int rmdPageSize, int imgWidth) throws AizouException, JsonProcessingException, IllegalAccessException, InstantiationException {
        DetailedPOIFormatter<? extends AbstractPOI> poiFormatter = new DetailedPOIFormatter<>(poiClass).setImageWidth(imgWidth);
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(spotId), poiClass, poiFormatter.getFilteredFields());
        if (poiInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));

        // 处理价格
        poiInfo.priceDesc = TaoziDataFilter.getPriceDesc(poiInfo);
        //是否被收藏
        MiscAPI.isFavorite(poiInfo, userId);
        JsonNode info = poiFormatter.format(poiInfo);

        //取得推荐
        List<POIRmd> rmdEntities = PoiAPI.getPOIRmd(spotId, rmdPage, rmdPageSize);
        POIRmdFormatter formatter = FormatterFactory.getInstance(POIRmdFormatter.class);
        JsonNode recommends = formatter.formatNode(rmdEntities);

        /*
           不要评论了 20150204
         */
        // 取得评论
        List<Comment> commentsEntities = MiscAPI.displayCommentApi(new ObjectId(spotId), null, null, 0, commentPage, commentPageSize);
        CommentFormatter comformatter = FormatterFactory.getInstance(CommentFormatter.class);
        JsonNode comments = comformatter.formatNode(commentsEntities);

        ObjectNode ret = (ObjectNode) info;
        ret.put("comments", comments);
        int commCnt = (int) PoiAPI.getPOICommentCount(spotId);
        ret.put("commentCnt", commCnt);

        if (poiClass == Shopping.class || poiClass == Restaurant.class) {
            // 添加H5接口 更多评论
            ret.put("moreCommentsUrl", "http://h5.taozilvxing.com/morecomment.php?pid=" + spotId);
            ret.put("recommends", recommends);
        }
        return ret;
    }

    /**
     * 获得POI的详细信息。
     *
     * @param poiDesc POI的类型说明:
     *                vs: 景点
     *                hotel: 酒店
     *                restaurant: 餐饮
     *                shopping:购物
     *                entertainment:美食
     * @param spotId  POI的ID。
     */
    @UsingCache(key = "poiInfo({poiId},{cmtPage},{cmtPageSize},{rmdPage},{rmdPageSize}", expireTime = 3600)
    public static Result viewPOIInfo(String poiDesc,
                                     @CacheKey(tag = "poiId") String spotId,
                                     @CacheKey(tag = "cmtPage") int commentPage,
                                     @CacheKey(tag = "cmtPageSize") int commentPageSize,
                                     @CacheKey(tag = "rmdPage") int rmdPage,
                                     @CacheKey(tag = "rmdPageSize") int rmdPageSize) {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        Class<? extends AbstractPOI> poiClass;
        switch (poiDesc) {
            case "vs":
                poiClass = ViewSpot.class;
                break;
            case "hotel":
                poiClass = Hotel.class;
                break;
            case "restaurant":
                poiClass = Restaurant.class;
                break;
            case "shopping":
                poiClass = Shopping.class;
                break;
            default:
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));
        }
        try {
            Long userId;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));
            else
                userId = null;
            JsonNode ret = viewPOIInfoImpl(poiClass, spotId, commentPage, commentPageSize, userId, rmdPage, rmdPageSize, imgWidth);
            return Utils.createResponse(ErrorCode.NORMAL, ret);
        } catch (AizouException | JsonProcessingException | InstantiationException | IllegalAccessException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 根据关键词搜索POI信息
     *
     * @param poiType
     * @param tag
     * @param keyword
     * @param page
     * @param pageSize
     * @param sortField
     * @param sortType
     * @param hotelTypeStr
     * @return
     */
    public static Result poiSearch(String poiType, String tag, String keyword, int page, int pageSize, String sortField, String sortType, String hotelTypeStr) {
        //酒店的类型
        int hotelType = 0;
        if (!hotelTypeStr.equals("")) {
            try {
                hotelType = Integer.parseInt(hotelTypeStr);
            } catch (ClassCastException e) {
                hotelType = 0;
            }

        }
        //判断搜索的关键词是否为空
        if (keyword.equals("")) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "key word can not be null");
        }
        PoiAPI.POIType type = null;
        switch (poiType) {
            case "vs":
                type = PoiAPI.POIType.VIEW_SPOT;
                break;
            case "hotel":
                type = PoiAPI.POIType.HOTEL;
                break;
            case "restaurant":
                type = PoiAPI.POIType.RESTAURANT;
                break;
            case "shopping":
                type = PoiAPI.POIType.SHOPPING;
                break;
        }
        if (type == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        //处理排序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;

        PoiAPI.SortField sf;
        switch (sortField) {
            case "price":
                sf = PoiAPI.SortField.PRICE;
                break;
            case "score":
                sf = PoiAPI.SortField.SCORE;
                break;
            default:
                sf = null;
        }

        List<JsonNode> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = null;
        try {
            it = PoiAPI.poiSearch(type, tag, keyword, sf, sort, page, pageSize, true, hotelType);
            while (it.hasNext())
                results.add(new SimplePOIFormatter().format(it.next()));
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 查看特定地区的poi
     *
     * @param poiType
     * @param locId
     * @param tagFilter
     * @param sortField
     * @param page
     * @param pageSize
     * @return
     */
    @UsingCache(key = "poiList({type},{loc},{sortField},{sortType},{page},{pageSize},{cmtPage},{cmtPageSize}",
            expireTime = 3600)
    public static Result viewPoiList(@CacheKey(tag = "type") String poiType,
                                     @CacheKey(tag = "loc") String locId, String tagFilter,
                                     @CacheKey(tag = "sortField") String sortField,
                                     @CacheKey(tag = "sortType") String sortType,
                                     @CacheKey(tag = "page") int page, @CacheKey(tag = "pageSize") int pageSize,
                                     @CacheKey(tag = "cmtPage") int commentPage,
                                     @CacheKey(tag = "cmtPageSize") int commentPageSize) {
        PoiAPI.POIType type = null;
        switch (poiType) {
            case "vs":
                type = PoiAPI.POIType.VIEW_SPOT;
                break;
            case "hotel":
                type = PoiAPI.POIType.HOTEL;
                break;
            case "restaurant":
                type = PoiAPI.POIType.RESTAURANT;
                break;
            case "shopping":
                type = PoiAPI.POIType.SHOPPING;
                break;
        }
        if (type == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));
        //处理排序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;
        PoiAPI.SortField sf;
        switch (sortField) {
            case "rating":
                sf = PoiAPI.SortField.RATING;
                break;
            case "hotness":
                sf = PoiAPI.SortField.HOTNESS;
                break;
            default:
                sf = null;
        }

        List<JsonNode> results = new ArrayList<>();
        List<? extends AbstractPOI> it;
        //List<Comment> commentsEntities;
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        try {
            it = PoiAPI.viewPoiList(type, new ObjectId(locId), sf, sort, page, pageSize);
            for (AbstractPOI temp : it) {
                temp.images = TaoziDataFilter.getOneImage(temp.images);
                temp.priceDesc = TaoziDataFilter.getPriceDesc(temp);
                //temp.desc = StringUtils.abbreviate(temp.desc, Constants.ABBREVIATE_LEN);
                ObjectNode ret = (ObjectNode) new SimplePOIFormatter().setImageWidth(imgWidth).format(temp);
                /*
                  Poi列表去掉评论和评论数 20150202
                 */
//                if (poiType.equals("restaurant") || poiType.equals("shopping") ||
//                        poiType.equals("hotel")) {
//                    commentsEntities = PoiAPI.getPOIComment(temp.getId().toString(), commentPage, commentPageSize);
//                    int commCnt = (int) PoiAPI.getPOICommentCount(temp.getId().toString());
//                    List<JsonNode> comments = new ArrayList<>();
//                    for (Comment cmt : commentsEntities) {
//                        comments.add(new CommentFormatter().format(cmt));
//                    }
//                    ret.put("comments", Json.toJson(comments));
//                    ret.put("commentCnt", commCnt);
//                }
                results.add(ret);
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 发现特定景点周边的poi
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getPoiNear(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                    boolean restaurant, boolean shopping, int page, int pageSize, int commentPage, int commentPageSize) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            ObjectNode results = getPoiNearImpl(lng, lat, maxDist, spot, hotel, restaurant, shopping, page, pageSize, commentPage, commentPageSize, imgWidth);
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    private static ObjectNode getPoiNearImpl(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                             boolean restaurant, boolean shopping, int page, int pageSize, int commentPage, int commentPageSize, int imgWidth) throws AizouException {
        ObjectNode results = Json.newObject();

        //发现poi
        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
        if (spot) {
            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
            poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");
        }

        if (hotel) {
            poiKeyList.add(PoiAPI.POIType.HOTEL);
            poiMap.put(PoiAPI.POIType.HOTEL, "hotel");
        }

        if (restaurant) {
            poiKeyList.add(PoiAPI.POIType.RESTAURANT);
            poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");
        }

        if (shopping) {
            poiKeyList.add(PoiAPI.POIType.SHOPPING);
            poiMap.put(PoiAPI.POIType.SHOPPING, "shopping");
        }

        for (PoiAPI.POIType poiType : poiKeyList) {
            List<JsonNode> retPoiList = new ArrayList<>();
            Iterator<? extends AbstractPOI> iterator = PoiAPI.getPOINearBy(poiType, lng, lat, maxDist,
                    page, pageSize);
            ObjectNode ret;
            AbstractPOI poi;
            List<Comment> commentsEntities;
            if (iterator != null) {
                for (; iterator.hasNext(); ) {
                    poi = iterator.next();
                    ret = (ObjectNode) new SimplePOIFormatter().setImageWidth(imgWidth).format(poi);
                    /*
                       我身边的POI列表去掉评论和评论数 20150202
                     */
//                    if (poiType.equals(PoiAPI.POIType.RESTAURANT) || poiType.equals(PoiAPI.POIType.SHOPPING) ||
//                            poiType.equals(PoiAPI.POIType.HOTEL)) {
//                        commentsEntities = PoiAPI.getPOIComment(poi.getId().toString(), commentPage, commentPageSize);
//                        int commCnt = (int) PoiAPI.getPOICommentCount(poi.getId().toString());
//                        List<JsonNode> comments = new ArrayList<>();
//                        for (Comment cmt : commentsEntities) {
//                            comments.add(new CommentFormatter().format(cmt));
//                        }
//                        ret.put("comments", Json.toJson(comments));
//                        ret.put("commentCnt", commCnt);
//                    }
                    retPoiList.add(ret);
                }
                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
            }

        }
        return results;
    }
//
//    /**
//     * 获取乘车指南/景点简介
//     *
//     * @param id
//     * @param desc
//     * @param traffic
//     * @return
//     */
//    public static Result getLocDetail(String id, boolean desc, boolean traffic) {
//        try {
//            ObjectNode results = Json.newObject();
//            ObjectId oid = new ObjectId(id);
//            ViewSpot viewSpot = PoiAPI.getVsDetail(oid, Arrays.asList(ViewSpot.detDesc));
//            if (desc) {
//                results.put("desc", viewSpot.description.desc);
//            }
//
//            if (traffic) {
//                results.put("traffic", viewSpot.description.traffic);
//            }
//            return Utils.createResponse(ErrorCode.NORMAL, results);
//        } catch (AizouException | NullPointerException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
//        }
//    }

    /**
     * 游玩攻略
     *
     * @param locId
     * @param field
     * @return
     */
    public static Result getTravelGuide(String locId, String field, String poiDesc) {
        try {
            List<String> destKeyList = new ArrayList<>();

            Class<? extends AbstractPOI> poiClass;
            switch (poiDesc) {
                case "vs":
                    poiClass = ViewSpot.class;
                    break;
                case "hotel":
                    poiClass = Hotel.class;
                    break;
                case "restaurant":
                    poiClass = Restaurant.class;
                    break;
                case "shopping":
                    poiClass = Shopping.class;
                    break;
                default:
                    return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));
            }

            switch (field) {
                case "tips":
                    destKeyList.add(AbstractPOI.FD_TIPS);
                    break;
                case "trafficInfo":
                    destKeyList.add(AbstractPOI.FD_TRAFFICINFO);
                    break;
                case "visitGuide":
                    destKeyList.add(AbstractPOI.FD_VISITGUIDE);
                    break;
            }

            AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(locId), poiClass, destKeyList);
            ObjectNode result = Json.newObject();
            if (field.equals("tips")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(GeoCtrl.contentsToList(poiInfo.getTips())));
            } else if (field.equals("trafficInfo")) {
                result.put("contents", Json.toJson(poiInfo.getTrafficInfo()));
            } else if (field.equals("visitGuide")) {
                result.put("contents", Json.toJson(poiInfo.getVisitGuide()));
            }

            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException | NullPointerException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}
