package controllers.taozi;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.taozi.user.DetailedPOIFormatter;
import utils.formatter.taozi.user.SelfFavoriteFormatter;
import utils.formatter.taozi.user.SimplePOIFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        JsonNode info = details ? new DetailedPOIFormatter().format(poiInfo) : new SimplePOIFormatter().format(poiInfo);
        ObjectNode ret = (ObjectNode) info;
        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }
}
