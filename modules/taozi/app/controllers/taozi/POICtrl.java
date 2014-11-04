package controllers.taozi;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.poi.AbstractPOI;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.taozi.user.DetailedPOIFormatter;
import utils.formatter.taozi.user.SimplePOIFormatter;

import java.util.ArrayList;
import java.util.Arrays;
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
     * @param gainType
     * @param lat
     * @param lgn
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getPOINearBy(String gainType, String lat, String lgn, int page, int pageSize) {

        try {
            Double latD = Double.valueOf(lat);
            Double lngD = Double.valueOf(lat);
            List<String> fieldsLimit = Arrays.asList(AbstractPOI.simpID, AbstractPOI.simpName, AbstractPOI.simpDesc, AbstractPOI.simpImg);
            List<AbstractPOI> poiInfos = (List<AbstractPOI>) PoiAPI.getPOINearBy(gainType, latD, lngD, fieldsLimit, page, pageSize);

            List<JsonNode>  nodeList = new ArrayList();
            for(AbstractPOI temp: poiInfos){
                nodeList.add( new SimplePOIFormatter().format(temp));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (NumberFormatException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }
}
