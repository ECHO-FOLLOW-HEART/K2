package controllers.taozi;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.poi.AbstractPOI;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.ProxyFormatter;
import utils.formatter.taozi.user.SimplePOIFormatter;

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
     * @param spotId      POI的ID。
     * @param showDetails 获得更多的详情。
     */
    public static Result viewPOIInfo(String poiDesc, String spotId, int showDetails, int pageSize) throws TravelPiException {

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
        JsonNode info = details ? new ProxyFormatter.DetailedPOIFormatter().format(poiInfo) : new SimplePOIFormatter().format(poiInfo);
        ObjectNode ret = (ObjectNode) info;
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }
}
