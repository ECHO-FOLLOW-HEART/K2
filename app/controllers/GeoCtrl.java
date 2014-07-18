package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.LocalityAPI;
import core.PoiAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.TravelPiBaseItem;
import models.geos.Locality;
import models.morphia.poi.AbstractPOI;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 地理相关
 * <p/>
 * Created by zephyre on 14-6-20.
 */
public class GeoCtrl extends Controller {
    public static JsonNode getLocProfile(Locality loc) {
        ObjectNode response = Json.newObject();
        response.put("id", loc.id);
        String enName = loc.enLocalityName;
        if (enName != null)
            response.put("enName", enName);
        String zhName = loc.zhLocalityName;
        if (zhName != null)
            response.put("zhName", zhName);
        response.put("level", loc.level);
        return response;
    }

    public static Result getLocalityByLong(long id) {
        Locality locality = Locality.finder.byId(id);
        if (locality == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality id: %d", id));

        ObjectNode response = (ObjectNode) getLocProfile(locality);

        Locality supLocality = locality.supLocality;
        if (supLocality != null)
            response.put("parent", getLocProfile(supLocality));

        List<JsonNode> siblingsList = new ArrayList<>();
        for (Locality sibling : locality.chiLocalityList)
            siblingsList.add(getLocProfile(sibling));
        response.put("siblings", Json.toJson(siblingsList));

        return Utils.createResponse(ErrorCode.NORMAL, response);
    }

    public static Result searchLocalityOld(String searchWord, int page, int pageSize) {
        List<Locality> locList = Locality.finder.where().ilike("localLocalityName", String.format("%%%s%%", searchWord))
                .setFirstRow(page * pageSize).setMaxRows(pageSize).findList();
        List<JsonNode> resultList = new ArrayList<>();
        for (Locality loc : locList) {
            ObjectNode item = Json.newObject();
            String tmp = loc.enLocalityName;
            if (tmp != null && !tmp.isEmpty())
                item.put("enName", tmp);
            tmp = loc.zhLocalityName;
            if (tmp != null && !tmp.isEmpty())
                item.put("zhName", tmp);
            item.put("id", loc.id);
            Locality supLocality = loc.supLocality;
            if (supLocality != null)
                item.put("parent", supLocality.id);
            resultList.add(item);
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(resultList));
    }


    /**
     * 搜索城市信息
     *
     * @param searchWord
     * @param prefix     是否为前缀搜索？
     * @param page
     * @param pageSize
     * @return
     */
    public static Result searchLocality(String searchWord, int prefix, int page, int pageSize) throws TravelPiException {
        List<JsonNode> results = new ArrayList<>();
        for (Iterator<models.morphia.geo.Locality> it =
                     LocalityAPI.searchLocalities(searchWord, (prefix != 0), page, pageSize);
             it.hasNext(); )
            results.add(it.next().toJson(1));

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    /**
     * 获得城市信息。同时查看相关的景点、酒店和餐厅。
     *
     * @param id    城市ID。
     * @param relVs 是否查看相关景点
     */
    public static Result getLocality(String id, int relVs, int relHotel, int relRestaurant) {
        try {
            ObjectNode result = (ObjectNode) LocalityAPI.locDetails(id, 3).toJson(3);

            int page = 0;
            int pageSize = 10;
            if (relVs != 0) {
                List<JsonNode> retVsNodes = new ArrayList<>();
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(PoiAPI.POIType.VIEW_SPOT, id, page, pageSize);
                     it.hasNext(); ) {
                    retVsNodes.add(it.next().toJson(2));
                }
                result.put("relVs", Json.toJson(retVsNodes));
            } else
            //edit by PC_Chen : return a empty list instead
//                result.put("relVs", "");
                result.put("relVs", Json.toJson(new ArrayList<JsonNode>()));
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result lookupLocality(int baiduId) throws TravelPiException {
        TravelPiBaseItem loc = LocalityAPI.locDetailsBaiduId(baiduId);
        if (loc == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Baidu ID: %d.", baiduId));
        else
            return Utils.createResponse(ErrorCode.NORMAL, loc.toJson());
    }

    /**
     * 通过百度ID得到城市信息。
     *
     * @param baiduId
     * @return
     */
    public static Result getLocalityBaiduId(int baiduId) {
        models.morphia.geo.Locality loc = null;
        try {
            loc = LocalityAPI.locDetailsBaiduId(baiduId);
            if (loc == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Baidu ID: %d.", baiduId));
            else
                return Utils.createResponse(ErrorCode.NORMAL, loc.toJson(1));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

    }
}
