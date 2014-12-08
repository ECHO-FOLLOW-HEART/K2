package controllers;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import exception.AizouException;
import exception.ErrorCode;
import formatter.travelpi.poi.BriefViewSpotFormatter;
import models.MorphiaFactory;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;

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

    private static JsonNode viewSpotInfoImpl(String poiDesc, String spotId, boolean showDetails, boolean showRelated,
                                             int pageSize) throws AizouException {
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
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));

        AbstractPOI poiInfo = PoiAPI.getPOIInfo(spotId, poiType, showDetails);
        if (poiInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));

        ObjectNode results = (ObjectNode) poiInfo.toJson(showDetails ? 3 : 2);

        if (showRelated) {
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

        return Json.toJson(results);
    }

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
    public static Result viewSpotInfo(String poiDesc, String spotId, boolean showDetails, boolean showRelated,
                                      int pageSize) throws AizouException {
        JsonNode result = viewSpotInfoImpl(poiDesc, spotId, showDetails, showRelated, pageSize);
        return Utils.createResponse(ErrorCode.NORMAL, result);
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

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);


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
     * 获得相关景点。
     *
     * @param spotId
     * @param tagFilter
     * @param sortFilter
     * @param sort
     * @param page
     * @param pageSize
     * @return
     * @throws UnknownHostException
     */
    public static Result relatedViewSpotListOld(String spotId, String tagFilter, String sortFilter, String sort, int page, int pageSize) throws UnknownHostException, AizouException {
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection("view_spot");
        String locId;
        try {
            DBObject vs = col.findOne(QueryBuilder.start("_id").is(new ObjectId(spotId)).get(),
                    BasicDBObjectBuilder.start("geo.locality._id", 1).get());
            locId = ((DBObject) ((DBObject) vs.get("geo")).get("locality")).get("_id").toString();
            if (locId == null)
                throw new NullPointerException();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid view spot ID: %s.", spotId));
        }

        return viewSpotList(locId, tagFilter, sortFilter, sort, page, pageSize);
    }

    /**
     * 获得相关景点。
     *
     * @param spotId     景点ID。
     * @param tag        相关景点的标签。
     * @param sortFilter
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    public static Result relatedViewSpotList(String spotId, String tag, String sortFilter, String sort, int page, int pageSize) {
        try {
            ViewSpot poiInfo = (ViewSpot) PoiAPI.getPOIInfo(spotId, PoiAPI.POIType.VIEW_SPOT, false);
            if (poiInfo == null)
                return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(new ArrayList<>()));

            List<JsonNode> vsList = new ArrayList<>();
            final ObjectId locId = poiInfo.addr.loc.id;
            for (Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(PoiAPI.POIType.VIEW_SPOT, locId,
                    tag, null, null, true, page, pageSize, false,
                    new HashMap<String, Object>() {
                        {
                            put("_id !=", locId);
                        }
                    }, 0); it.hasNext(); ) {
                ViewSpot vs = (ViewSpot) it.next();
                vsList.add(vs.toJson(2));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(vsList));
        } catch (NullPointerException e) {
            return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "");
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
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

    public static JsonNode poiSearchImpl(String poiType, String locId, String tag, String keyword, int page, int pageSize,
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
        }
        if (type == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

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

        ObjectId locOid;
        if (locId == null)
            locOid = null;
        else {
            try {
                locOid = new ObjectId(locId);
            } catch (IllegalArgumentException e) {
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s", locId));
            }
        }
        List<JsonNode> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(type, locOid, tag, keyword, sf, sort, page, pageSize, true, null, hotelType);
        while (it.hasNext()) {
            results.add(BriefViewSpotFormatter.getInstance().format(it.next()));
        }
//            results.add(it.next().toJson(2));

        return Json.toJson(results);
    }

    /**
     * 对POI进行签到操作。
     *
     * @param poiType POI类型。
     * @return
     */
    public static Result poiCheckin(String poiType, String poiId) {
        return Utils.createResponse(ErrorCode.NORMAL, "SUCCESS");
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
