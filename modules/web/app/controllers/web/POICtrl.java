package controllers.web;

import aizou.core.PoiAPI;
import aizou.core.WebPoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import exception.AizouException;
import exception.ErrorCode;
import models.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.web.poi.WebPOIFormatter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * POI相关信息。
 *
 * @author Zephyre
 */
public class POICtrl extends Controller {

    /**
     * 获得POI的详细信息。
     *
     * @param poiDesc     POI的类型说明:
     *                    vs: 景点
     *                    hotel: 酒店
     *                    restaurant: 餐饮
     * @param spotId      POI的ID。
     * @param showDetails 获得更多的详情。
     * @param showRelated 获得相关POI信息。
     */
    public static Result viewSpotInfo(String poiDesc, String spotId, int showDetails, int showRelated, int pageSize) throws AizouException {

        PoiAPI.POIType poiType = null;
        switch (poiDesc) {
            case "vs":
                poiType = PoiAPI.POIType.VIEW_SPOT;
                break;
            case "hotel":
                poiType = PoiAPI.POIType.HOTEL;
                break;
            case "restaurant":
                poiType = PoiAPI.POIType.RESTAURANT;
                break;
        }
        if (poiType == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));

        boolean details = (showDetails != 0);
        AbstractPOI poiInfo = WebPoiAPI.getPOIInfo(spotId, poiType, details);
        if (poiInfo == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));

        ObjectNode results = (ObjectNode) poiInfo.toJson(details ? 3 : 2);

        if (showRelated != 0) {
            // 获得相关景点
            try {
                List<JsonNode> vsList = new ArrayList<>();
                final ObjectId locId = poiInfo.addr.loc.id;
                final ObjectId vsId = poiInfo.getId();
                for (Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(poiType, locId,
                        null, null, null, true, 0, pageSize, false,
                        new HashMap<String, Object>() {
                            {
                                put("_id !=", vsId);
                                put("imageList !=", null);
                            }
                        }, 0); it.hasNext(); ) {
                    AbstractPOI vs = it.next();
                    vsList.add(vs.toJson(1));
                }
                results.put("related", Json.toJson(vsList));
            } catch (NullPointerException e) {
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
            } catch (AizouException e) {
                throw new AizouException(e.getErrCode(), e.getMessage());
            }
        }
        JsonNode result = DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC);
        return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appDescFilter(result, request()));
    }


    public static Result viewSpotList(String locality, String tagFilter, String sortFilter, String sort,
                                      int page, int pageSize) throws UnknownHostException, AizouException {
        return poiList("vs", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result hotelList(String locality, String tagFilter, String sortFilter, String sort,
                                   int page, int pageSize) throws UnknownHostException, AizouException {
        return poiList("hotel", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result restaurantList(String locality, String tagFilter, String sortFilter, String sort,
                                        int page, int pageSize) throws UnknownHostException, AizouException {
        return poiList("restaurant", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    /**
     * 搜索景点的ID。
     *
     * @param locality   景点所在地区的ID。
     * @param tagFilter  按照标签对景点进行过滤。
     * @param sortFilter 景点列表排序的字段，目前仅支持rating，即按照评分进行排序。
     * @param sort       升序(asc)还是降序(desc)
     * @param page       起始页码。
     * @param pageSize   页面大小。
     */
    private static Result poiList(String poiType, String locality, String tagFilter, String sortFilter, String sort,
                                  int page, int pageSize) throws UnknownHostException, AizouException {
        String colName = null;
        switch (poiType) {
            case "vs":
                colName = "view_spot";
                break;
            case "hotel":
                colName = "hotel";
                break;
            case "restaurant":
                colName = "restaurant";
                break;
        }
        if (colName == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);

        BasicDBObject query = new BasicDBObject();
        try {
            query.put("geo.locality._id", new ObjectId(locality));
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locality));
        }
        if (tagFilter != null && !tagFilter.isEmpty())
            query.put("tags", tagFilter);
        DBCursor cursor = col.find(query).skip(page * pageSize).limit(pageSize);
        if (sort.equals("asc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", 1));
        else if (sort.equals("desc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", -1));

        List<JsonNode> resultList = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject item = cursor.next();
            ObjectNode node = Json.newObject();
            node.put("_id", item.get("_id").toString());

            DBObject tmp = (DBObject) item.get("ratings");
            if (tmp != null)
                node.put("ratings", Json.toJson(tmp));
            tmp = (DBObject) item.get("tags");
            if (tmp != null)
                node.put("tags", Json.toJson(tmp));

            String name = (String) item.get("name");
            if (name != null)
                node.put("name", name);

            try {
                String desc = (String) ((DBObject) item.get("intro")).get("desc");
                if (desc != null) {
                    desc = StringUtils.abbreviate(desc, 64);
                    node.put("desc", desc);
                }
            } catch (NullPointerException ignore) {
            }

            tmp = (DBObject) item.get("geo");
            if (tmp != null) {
                DBObject loc = (DBObject) tmp.get("locality");
                String locId = loc.get("_id").toString();
                loc.put("_id", locId);
                node.put("geo", Json.toJson(tmp));
            }

            // TODO 此处的imageList和images，应该用哪一个？
            tmp = (DBObject) item.get("imageList");
            if (tmp != null)
                node.put("imageList", Json.toJson(tmp));

            resultList.add(node);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(resultList));
    }

    /**
     * 根据关键词搜索POI信息。
     *
     * @param poiType
     * @param locId
     * @param keyword
     * @param page
     * @param pageSize
     * @return
     */
    public static Result poiSearch(String poiType, String locId, String tag, String keyword, int page, int pageSize,
                                   String sortField, String sortType, String hotelTypeStr) {
        try {
            JsonNode results = poiSearchImpl(poiType, locId, tag, keyword, page, pageSize, sortField, sortType,
                    hotelTypeStr);
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    private static JsonNode poiSearchImpl(String poiType, String locId, String tag, String keyword, int page, int pageSize,
                                          String sortField, String sortType, String hotelTypeStr) throws AizouException {
        if (locId.isEmpty())
            locId = null;
        int hotelType = 0;
        if (!hotelTypeStr.equals("")) {
            try {
                hotelType = Integer.parseInt(hotelTypeStr);
            } catch (ClassCastException e) {
                hotelType = 0;
            }
        }

        Class<? extends AbstractPOI> poiClass = null;
        PoiAPI.POIType type = null;
        switch (poiType) {
            case "vs":
                type = PoiAPI.POIType.VIEW_SPOT;
                poiClass = ViewSpot.class;
                break;
            case "hotel":
                type = PoiAPI.POIType.HOTEL;
                poiClass = Hotel.class;
                break;
            case "restaurant":
                type = PoiAPI.POIType.RESTAURANT;
                poiClass = Restaurant.class;
                break;
            case "shopping":
                type = PoiAPI.POIType.SHOPPING;
                poiClass = Shopping.class;
                break;
        }
        if (type == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        //排序顺序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;

        //排序字段
        PoiAPI.SortField sf;
        switch (sortField) {
            case "hotness":
                sf = PoiAPI.SortField.HOTNESS;
                break;
            case "rating":
                sf = PoiAPI.SortField.RATING;
                break;
            default:
                sf = null;
        }

        ObjectId locOid = null;
        try {
            if (locId != null)
                locOid = new ObjectId(locId);
        } catch (IllegalArgumentException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s", locId));
        }
        List<JsonNode> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(type, locOid, tag, keyword, sf, sort, page, pageSize, true, null, hotelType);
        while (it.hasNext())
            results.add(new WebPOIFormatter(poiClass).format(it.next()));

        return Json.toJson(results);
    }

    /**
     *
     */
    public static Result explore(String poiType, String locId, int page, int pageSize) throws AizouException {

        ObjectId oLocId = locId.equals("") ? null : new ObjectId(locId);
        PoiAPI.POIType pt = null;

        if (poiType.equals("vs"))
            pt = PoiAPI.POIType.VIEW_SPOT;
        if (poiType.equals("hotel"))
            pt = PoiAPI.POIType.HOTEL;
        if (poiType.equals("restaurant"))
            pt = PoiAPI.POIType.RESTAURANT;

        List<JsonNode> retPoiList = new ArrayList<>();

        for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(pt, oLocId, false, page, pageSize); it.hasNext(); )
            retPoiList.add(it.next().toJson(2));

        return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(retPoiList), request(), Constants.SMALL_PIC));

    }
}
