package controllers.app;

import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import aspectj.Key;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2014/11/1.
 */
public class POICtrl extends Controller {

    public static JsonNode viewPOIInfoImpl(String poiDesc, Class<? extends AbstractPOI> poiClass, Class<? extends Comment> commitClass, String spotId,
                                           int commentPage, int commentPageSize, Long userId,
                                           int rmdPage, int rmdPageSize, int imgWidth)
            throws AizouException {
        DetailedPOIFormatter<? extends AbstractPOI> poiFormatter = FormatterFactory.getInstance(DetailedPOIFormatter.class, imgWidth);
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(spotId), poiClass, poiFormatter.getFilteredFields(poiClass));
        if (poiInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));

        // 处理价格
        //poiInfo.priceDesc = TaoziDataFilter.getPriceDesc(poiInfo);
        //poiInfo.desc = StringUtils.abbreviate(poiInfo.desc, Constants.ABBREVIATE_LEN);
        //是否被收藏
        MiscAPI.isFavorite(poiInfo, userId);
        JsonNode info = poiFormatter.formatNode(poiInfo);

        //取得推荐
        List<POIRmd> rmdEntities = PoiAPI.getPOIRmd(spotId, rmdPage, rmdPageSize);
        POIRmdFormatter formatter = FormatterFactory.getInstance(POIRmdFormatter.class);
        JsonNode recommends = formatter.formatNode(rmdEntities);

        // 取得评论

        List<Comment> commentsEntities = MiscAPI.getComments(commitClass, new ObjectId(spotId), null, null, 0, commentPage, commentPageSize);
        CommentFormatter comformatter = FormatterFactory.getInstance(CommentFormatter.class);
        JsonNode comments = comformatter.formatNode(commentsEntities);

        ObjectNode ret = (ObjectNode) info;
        ret.put("comments", comments);
        int commCnt = (int) PoiAPI.getPOICommentCount(spotId);
        ret.put("commentCnt", commCnt);


        if (poiClass == Shopping.class || poiClass == Restaurant.class) {
            // 添加H5接口 更多评论
            ret.put("moreCommentsUrl", "http://h5.taozilvxing.com/poi/morecomment.php?pid=" + spotId + "&poiType=" + poiDesc);
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
    //@UsingOcsCache(key = "poiInfo({poiId},{cmtPage},{cmtPageSize},{rmdPage},{rmdPageSize},{isWeb}", expireTime = 3600)
    public static Result viewPOIInfo(String poiDesc,
                                     @Key(tag = "poiId") String spotId,
                                     @Key(tag = "cmtPage") int commentPage,
                                     @Key(tag = "cmtPageSize") int commentPageSize,
                                     @Key(tag = "rmdPage") int rmdPage,
                                     @Key(tag = "rmdPageSize") int rmdPageSize) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        Class<? extends AbstractPOI> poiClass;
        Class<? extends Comment> commentClass = Comment.class;
        switch (poiDesc) {
            case "vs":
                poiClass = ViewSpot.class;
                break;
            case "hotel":
                poiClass = Hotel.class;
                break;
            case "restaurant":
                poiClass = Restaurant.class;
                commentClass = RestaurantComment.class;
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
        JsonNode ret = viewPOIInfoImpl(poiDesc, poiClass, commentClass, spotId, commentPage, commentPageSize, userId, rmdPage, rmdPageSize, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    public static Result poiSearch(String poiType, String query, String locality, double lng, double lat, double maxDist,
                                   String tag, String hotelTypeStr,
                                   String sortBy, String sort, int page, int pageSize) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        //限定目的地ID
        ObjectId locId = null;
        if (locality != null && !locality.equals(""))
            locId = new ObjectId(locality);
        //酒店的类型
        int hotelType = 0;
        if (!hotelTypeStr.equals("")) {
            try {
                hotelType = Integer.parseInt(hotelTypeStr);
            } catch (ClassCastException e) {
                hotelType = 0;
            }
        }

        PoiAPI.POIType type = null;
        switch (poiType) {
            case "viewspot":
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
        boolean isSortAsc = false;
        if (sort != null && sort.equals("asc"))
            isSortAsc = true;

        PoiAPI.SortField sf;
        switch (sortBy) {
            case "price":
                sf = PoiAPI.SortField.PRICE;
                break;
            default:
                sf = null;
        }

        List<AbstractPOI> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(type, query, locId, lng, lat, maxDist, tag, hotelType, sf, isSortAsc, page, pageSize);
        while (it.hasNext())
            results.add(it.next());
        SimplePOIFormatter<? extends AbstractPOI> simplePOIFormatter = FormatterFactory.getInstance(SimplePOIFormatter.class, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, simplePOIFormatter.formatNode(results));
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


