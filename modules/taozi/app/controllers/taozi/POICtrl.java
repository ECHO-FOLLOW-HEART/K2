package controllers.taozi;

import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Key;
import controllers.UsingOcsCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.DetailsEntryFormatter;
import formatter.taozi.misc.CommentFormatter;
import formatter.taozi.poi.DetailedPOIFormatter;
import formatter.taozi.poi.POIRmdFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import models.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.TaoziDataFilter;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2014/11/1.
 */
public class POICtrl extends Controller {

    public static JsonNode viewPOIInfoImpl(Class<? extends AbstractPOI> poiClass, String spotId,
                                           int commentPage, int commentPageSize, Long userId,
                                           int rmdPage, int rmdPageSize, int imgWidth, boolean isWeb)
            throws AizouException {
        DetailedPOIFormatter<? extends AbstractPOI> poiFormatter = FormatterFactory.getInstance(DetailedPOIFormatter.class, imgWidth);
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(spotId), poiClass, poiFormatter.getFilteredFields(poiClass));
        if (poiInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));

        // 处理价格
        //poiInfo.priceDesc = TaoziDataFilter.getPriceDesc(poiInfo);
        if (!isWeb)
            poiInfo.desc = StringUtils.abbreviate(poiInfo.desc, Constants.ABBREVIATE_LEN);
        //是否被收藏
        MiscAPI.isFavorite(poiInfo, userId);
        JsonNode info = poiFormatter.formatNode(poiInfo);

        //取得推荐
        List<POIRmd> rmdEntities = PoiAPI.getPOIRmd(spotId, rmdPage, rmdPageSize);
        POIRmdFormatter formatter = FormatterFactory.getInstance(POIRmdFormatter.class);
        JsonNode recommends = formatter.formatNode(rmdEntities);

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
        } else if (poiClass == ViewSpot.class) {
            // 获得同城的销售数据
            LyMapping lyMapping = PoiAPI.getTongChenPOI(poiInfo.getId());
            if (lyMapping == null)
                ret.put("lyPoiUrl", "");
            else
                ret.put("lyPoiUrl", String.format("http://m.ly.com/scenery/scenerydetail_%s_0_0.html", lyMapping.getLyId()));
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
    @UsingOcsCache(key = "poiInfo({poiId},{cmtPage},{cmtPageSize},{rmdPage},{rmdPageSize},{isWeb}", expireTime = 3600)
    public static Result viewPOIInfo(String poiDesc,
                                     @Key(tag = "poiId") String spotId,
                                     @Key(tag = "cmtPage") int commentPage,
                                     @Key(tag = "cmtPageSize") int commentPageSize,
                                     @Key(tag = "rmdPage") int rmdPage,
                                     @Key(tag = "rmdPageSize") int rmdPageSize,
                                     @Key(tag = "isWeb") boolean isWeb) throws AizouException {
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

        Long userId;
        if (request().hasHeader("UserId"))
            userId = Long.parseLong(request().getHeader("UserId"));
        else
            userId = null;
        JsonNode ret = viewPOIInfoImpl(poiClass, spotId, commentPage, commentPageSize, userId, rmdPage, rmdPageSize, imgWidth, isWeb);
        return Utils.createResponse(ErrorCode.NORMAL, ret);
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
    public static Result poiSearch(String poiType, String tag, String keyword, int page, int pageSize,
                                   String sortField, String sortType, String hotelTypeStr) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

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

        List<AbstractPOI> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = null;

        it = PoiAPI.poiSearch(type, tag, keyword, sf, sort, page, pageSize, true, hotelType);
        while (it.hasNext())
            results.add(it.next());
        SimplePOIFormatter<? extends AbstractPOI> simplePOIFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, simplePOIFormatter.formatNode(results));
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
    @UsingOcsCache(key = "poiList({type},{loc},{sortField},{sortType},{page},{pageSize},{cmtPage},{cmtPageSize}",
            expireTime = 3600)
    public static Result viewPoiList(@Key(tag = "type") String poiType,
                                     @Key(tag = "loc") String locId, String tagFilter,
                                     @Key(tag = "sortField") String sortField,
                                     @Key(tag = "sortType") String sortType,
                                     @Key(tag = "page") int page, @Key(tag = "pageSize") int pageSize,
                                     @Key(tag = "cmtPage") int commentPage,
                                     @Key(tag = "cmtPageSize") int commentPageSize) throws AizouException {
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

        List<AbstractPOI> results = new ArrayList<>();
        List<? extends AbstractPOI> it;
        //List<Comment> commentsEntities;
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        it = PoiAPI.viewPoiList(type, new ObjectId(locId), sf, sort, page, pageSize);
        for (AbstractPOI temp : it) {
            temp.images = TaoziDataFilter.getOneImage(temp.images);
            temp.priceDesc = TaoziDataFilter.getPriceDesc(temp);
            //temp.desc = StringUtils.abbreviate(temp.desc, Constants.ABBREVIATE_LEN);
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
            results.add(temp);

        }
        SimplePOIFormatter simplePOIFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, simplePOIFormatter.formatNode(results));
    }

    /**
     * 发现特定景点周边的poi
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getPoiNear(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                    boolean restaurant, boolean shopping, int page, int pageSize,
                                    int commentPage, int commentPageSize) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        ObjectNode results = getPoiNearImpl(lng, lat, maxDist, spot, hotel, restaurant, shopping, page, pageSize, commentPage, commentPageSize, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, results);
    }

    private static ObjectNode getPoiNearImpl(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                             boolean restaurant, boolean shopping, int page, int pageSize,
                                             int commentPage, int commentPageSize, int imgWidth)
            throws AizouException {
        ObjectNode results = Json.newObject();
        Class<? extends AbstractPOI> poiClass = null;
        //发现poi
        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
        if (spot) {
            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
            poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");
            poiClass = ViewSpot.class;
        }

        if (hotel) {
            poiKeyList.add(PoiAPI.POIType.HOTEL);
            poiMap.put(PoiAPI.POIType.HOTEL, "hotel");
            poiClass = Hotel.class;
        }

        if (restaurant) {
            poiKeyList.add(PoiAPI.POIType.RESTAURANT);
            poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");
            poiClass = Restaurant.class;
        }

        if (shopping) {
            poiKeyList.add(PoiAPI.POIType.SHOPPING);
            poiMap.put(PoiAPI.POIType.SHOPPING, "shopping");
            poiClass = Shopping.class;
        }

        for (PoiAPI.POIType poiType : poiKeyList) {
            List<AbstractPOI> retPoiList = new ArrayList<>();
            Iterator<? extends AbstractPOI> iterator = PoiAPI.getPOINearBy(poiType, lng, lat, maxDist,
                    page, pageSize);
            ObjectNode ret;
            AbstractPOI poi;
            List<Comment> commentsEntities;
            if (iterator != null) {
                for (; iterator.hasNext(); ) {
                    poi = iterator.next();
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
                    retPoiList.add(poi);
                }
                SimplePOIFormatter simplePOIFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
                results.put(poiMap.get(poiType), simplePOIFormatter.formatNode(retPoiList));
            }

        }
        return results;
    }


    /**
     * 游玩攻略
     *
     * @param locId
     * @param field
     * @return
     */
    public static Result getTravelGuide(String locId, String field, String poiDesc) throws AizouException {
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
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(locId), poiClass, destKeyList);
        ObjectNode result = Json.newObject();
        switch (field) {
            case "tips":
                result.put("desc", "");
                DetailsEntryFormatter detailsEntryFormatter = FormatterFactory.getInstance(DetailsEntryFormatter.class, imgWidth);
                result.put("contents", poiInfo.getTips() == null ? Json.toJson(new ArrayList<>()) : detailsEntryFormatter.formatNode(poiInfo.getTips()));
                break;
            case "trafficInfo":
                result.put("contents", Json.toJson(poiInfo.getTrafficInfo()));
                break;
            case "visitGuide":
                result.put("contents", Json.toJson(poiInfo.getVisitGuide()));
                break;
        }

        return Utils.createResponse(ErrorCode.NORMAL, result);
    }
}


