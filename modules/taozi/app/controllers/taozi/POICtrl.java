package controllers.taozi;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.poi.AbstractPOI;
import models.poi.TravelGuide;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.taozi.user.DetailedPOIFormatter;
import utils.formatter.taozi.user.SimplePOIFormatter;

import java.util.*;

/**
 * Created by topy on 2014/11/1.
 */
public class POICtrl extends Controller {

    /**
     * 获得POI的详细信息。
     *
     * @param poiDesc     POI的类型说明:
     *                    vs: 景点
     *                    hotel: 酒店
     *                    restaurant: 餐饮
     *                    shopping:购物
     *                    entertainment:美食
     * @param spotId      POI的ID。
     * @param showDetails 获得更多的详情。
     */
    public static Result viewPOIInfo(String poiDesc, String spotId, int showDetails, int pageSize) {
        try {
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
                case "shopping":
                    poiType = PoiAPI.POIType.SHOPPING;
                    break;
                case "entertainment":
                    poiType = PoiAPI.POIType.ENTERTAINMENT;
                    break;
            }
            if (poiType == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));

            boolean details = (showDetails != 0);
            AbstractPOI poiInfo = PoiAPI.getPOIInfo(spotId, poiType, details);
            if (poiInfo == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));
            JsonNode info = details ? new DetailedPOIFormatter().format(poiInfo) : new SimplePOIFormatter().format(poiInfo);
            ObjectNode ret = (ObjectNode) info;
            return Utils.createResponse(ErrorCode.NORMAL, ret);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 获得景点周边的周边POI按照
     *
     * @param gainType
     * @param lat
     * @param lgn
     * @param page
     * @param pageSize
     * @return
     *//*
    public static Result getPOINearBy(String gainType, String lat, String lgn, int page, int pageSize) {

        try {
            Double latD = Double.valueOf(lat);
            Double lngD = Double.valueOf(lat);
            List<String> fieldsLimit = Arrays.asList(AbstractPOI.simpID, AbstractPOI.simpName, AbstractPOI.simpDesc, AbstractPOI.simpImg);
            List<AbstractPOI> poiInfos = (List<AbstractPOI>) PoiAPI.getPOINearBy(gainType, latD, lngD, fieldsLimit, page, pageSize);

            List<JsonNode> nodeList = new ArrayList();
            for (AbstractPOI temp : poiInfos) {
                nodeList.add(new SimplePOIFormatter().format(temp));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (NumberFormatException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }*/

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
                results.add(new DetailedPOIFormatter().format(it.next()));
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
        } catch (TravelPiException | NullPointerException e) {
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
    public static Result viewPoiList(String poiType, String locId, String tagFilter, String sortField, String sortType, int page, int pageSize) {
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
            it = PoiAPI.poiList(type, new ObjectId(locId), tagFilter, sf, sort, true, page, pageSize);
            while (it.hasNext())
                results.add(new DetailedPOIFormatter().format(it.next()));
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
        } catch (TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    public static Result viewSpotList(String locId, String tagFilter, String sortField,
                                      String sortType, int page, int pageSize)
            throws TravelPiException {
        return viewPoiList("vs", locId, tagFilter, sortField, sortType, page, pageSize);
    }

    public static Result viewHotelList(String locId, String tagFilter, String sortField,
                                       String sortType, int page, int pageSize)
            throws TravelPiException {
        return viewPoiList("hotel", locId, tagFilter, sortField, sortType, page, pageSize);
    }

    public static Result viewRestaurantList(String locId, String tagFilter, String sortField,
                                            String sortType, int page, int pageSize)
            throws TravelPiException {
        return viewPoiList("restaurant", locId, tagFilter, sortField, sortType, page, pageSize);
    }

    /**
     * 发现特定景点周边的poi
     *
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static Result getPoiNear(double lng, double lat, boolean spot, boolean hotel, boolean restaurant, int page, int pageSize) {
        try {
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
                poiKeyList.add(PoiAPI.POIType.HOTEL);
                poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");
            }

            for (PoiAPI.POIType poiType : poiKeyList) {
                List<JsonNode> retPoiList = new ArrayList<>();
                Iterator<? extends AbstractPOI> iterator = PoiAPI.getPOINearBy(poiType, lng, lat, page, pageSize);
                if (iterator != null) {
                    for (; iterator.hasNext(); )
                        retPoiList.add(new DetailedPOIFormatter().format(iterator.next()));
                    results.put(poiMap.get(poiType), Json.toJson(retPoiList));
                }

            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (TravelPiException | NullPointerException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 获取乘车指南/景点简介
     *
     * @param id
     * @param desc
     * @param traffic
     * @return
     */
    public static Result getViewSpotDetail(String id, Boolean desc, Boolean traffic) {
        try {
            ObjectNode results = Json.newObject();
            ObjectId oid = new ObjectId(id);
            ViewSpot viewSpot = PoiAPI.getVsDetail(oid, Arrays.asList(ViewSpot.detDesc));
            if (desc) {
                results.put("desc", viewSpot.description.desc);
            }

            if (traffic) {
                results.put("traffic", viewSpot.description.traffic);
            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    public static Result getTravelGuide(String locId, Boolean arrive, Boolean traffic, Boolean bright, Boolean activity, Boolean tips, Boolean culture) {
        try {
            ObjectNode results = Json.newObject();
            TravelGuide travelGuide = PoiAPI.getTravelGuideApi(new ObjectId(locId));
            if (arrive) {
                results.put("arrive", travelGuide.arrive);
            }
            if (traffic) {
                results.put("traffic", travelGuide.traffic);
            }
            if (bright) {
                results.put("bright", travelGuide.bright);
            }
            if (activity) {
                results.put("activity", travelGuide.activity);
            }
            if (tips) {
                results.put("tips", travelGuide.tips);
            }
            if (culture) {
                results.put("culture", travelGuide.cultrue);
            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}
