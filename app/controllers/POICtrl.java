package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.PoiAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.poi.AbstractPOI;
import models.morphia.poi.ViewSpot;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
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

    /**
     * 获得POI的详细信息。
     *
     * @param poiDesc POI的类型说明:
     *                vs: 景点
     *                hotel: 酒店
     *                restaurant: 餐饮
     * @param spotId      POI的ID。
     * @param showDetails 获得更多的详情。
     * @param showRelated 获得相关POI信息。
     */
    public static Result viewSpotInfo(String poiDesc, String spotId, int showDetails, int showRelated, int pageSize) throws TravelPiException {

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
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(spotId, poiType, details);
        ObjectNode results = (ObjectNode) poiInfo.toJson(details ? 3 : 2);

        if (showRelated != 0) {
            // 获得相关景点
            try {
                List<JsonNode> vsList = new ArrayList<>();
                final ObjectId locId = poiInfo.addr.loc.id;
                final ObjectId vsId = poiInfo.id;
                for (Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(poiType, locId,
                        null, null, null, true, 0, pageSize, false,
                        new HashMap<String, Object>() {
                            {
                                put("_id !=", vsId);
                            }
                        },0); it.hasNext(); ) {
                    AbstractPOI vs = it.next();
                    vsList.add(vs.toJson(1));
                }
                results.put("related", Json.toJson(vsList));
            } catch (NullPointerException e) {
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
            } catch (TravelPiException e) {
                throw new TravelPiException(e.errCode, e.getMessage());
            }
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    public static Result viewSpotList(String locality, String tagFilter, String sortFilter, String sort,
                                      int page, int pageSize) throws UnknownHostException, TravelPiException {
        return poiList("vs", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result hotelList(String locality, String tagFilter, String sortFilter, String sort,
                                   int page, int pageSize) throws UnknownHostException, TravelPiException {
        return poiList("hotel", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result restaurantList(String locality, String tagFilter, String sortFilter, String sort,
                                        int page, int pageSize) throws UnknownHostException, TravelPiException {
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
                                  int page, int pageSize) throws UnknownHostException, TravelPiException {
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
    public static Result relatedViewSpotListOld(String spotId, String tagFilter, String sortFilter, String sort, int page, int pageSize) throws UnknownHostException, TravelPiException {
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
                    },0); it.hasNext(); ) {
                ViewSpot vs = (ViewSpot) it.next();
                vsList.add(vs.toJson(2));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(vsList));
        } catch (NullPointerException e) {
            return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
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
    public static Result poiSearch(String poiType, String locId, String tag, String keyword, int page, int pageSize,String sortField, String sortType,String hotelTypeStr) {
        if (locId.isEmpty())
            locId = null;
        int hotelType = 0;
        if(!hotelTypeStr.equals("")){
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
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        //处理排序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;

        PoiAPI.SortField sf = PoiAPI.SortField.PRICE;
        switch (sortField) {
            case "price":
                sf = PoiAPI.SortField.PRICE;
                break;
            case "score":
                sf = PoiAPI.SortField.SCORE;
                break;
        }

        try {
            ObjectId locOid;
            if (locId == null)
                locOid = null;
            else {
                try {
                    locOid = new ObjectId(locId);
                } catch (IllegalArgumentException e) {
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s", locId));
                }
            }
            List<JsonNode> results = new ArrayList<>();
            Iterator<? extends AbstractPOI> it = PoiAPI.poiSearch(type, locOid, tag, keyword, sf, sort, page, pageSize, true, null,hotelType);
            while (it.hasNext())
                results.add(it.next().toJson(2));

            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
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
}
