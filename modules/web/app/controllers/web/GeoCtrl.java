package controllers.web;

import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.geo.Locality;
import models.geo.Country;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.travelpi.geo.LocalityFormatter;
import utils.formatter.travelpi.geo.SimpleLocalityFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;


/**
 * 地理相关
 * <p/>
 * Created by zephyre on 14-6-20.
 */
public class GeoCtrl extends Controller {

    /**
     * 搜索城市信息。
     *
     * @param searchWord 搜索关键词。
     * @param country    限定国家或地区的ID。//要求为国家ISO 3166代码，如CN或CHN。如果为null或""，则不限定国家。
     * @param scope      搜索国外城市还是国内城市。1：国内，2：国外，3：both。
     * @param prefix     是否为前缀搜索。
     */
    public static Result searchLocality(String searchWord, String country, int scope, boolean prefix, int page, int pageSize) {
        if (scope < 1 || scope > 3)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid search scope: %d", scope));

        searchWord = (searchWord != null ? searchWord.trim() : "");
        country = (country != null ? country.trim() : "");

        ObjectId countryId = null;
        if (!country.isEmpty()) {
            try {
                countryId = new ObjectId(country);
            } catch (IllegalArgumentException e) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid ObjectId: %s", country));
            }
        }

        List<JsonNode> results = new ArrayList<>();
        try {
            for (Iterator<models.geo.Locality> it =
                         LocalityAPI.searchLocalities(searchWord, countryId, scope, prefix, page, pageSize);
                 it.hasNext(); )
                results.add(SimpleLocalityFormatter.getInstance().format(it.next()));
        } catch (PatternSyntaxException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "KeyWord Pattern Error.");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

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
            models.geo.Locality loc = LocalityAPI.locDetails(id, 3);
            if (loc==null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");

            ObjectNode result = (ObjectNode) LocalityFormatter.getInstance().format(loc);

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

//    public static Result lookupLocality(int baiduId) throws TravelPiException {
//        models.geo.Locality loc = LocalityAPI.locDetailsBaiduId(baiduId);
//        if (loc == null)
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Baidu ID: %d.", baiduId));
//        else
//            return Utils.createResponse(ErrorCode.NORMAL, loc.toJson());
//    }

//    /**
//     * 通过百度ID得到城市信息。
//     *
//     * @param baiduId
//     * @return
//     */
//    public static Result getLocalityBaiduId(int baiduId) {
//        models.geo.Locality loc = null;
//        try {
//            loc = LocalityAPI.locDetailsBaiduId(baiduId);
//            if (loc == null)
//                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Baidu ID: %d.", baiduId));
//            else
//                return Utils.createResponse(ErrorCode.NORMAL, loc.toJson(1));
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.errCode, e.getMessage());
//        }
//
//    }

    /**
     * 根据ID获得
     *
     * @param id
     * @return
     */
    public static Result getCountry(String id) {
        try {
            Country country = LocalityAPI.countryDetails(id);
            return Utils.createResponse(ErrorCode.NORMAL, country.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 搜索国家信息
     *
     * @param keyword
     * @param searchType
     * @param page
     * @param pageSize
     * @return
     */
    public static Result searchCountry(String keyword, String searchType, int page, int pageSize) {
//        if (keyword == null || keyword.trim().isEmpty())
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid keyword");
        if (searchType == null || searchType.trim().isEmpty())
            searchType = "name";

        try {
            List<Country> countryList;
            switch (searchType) {
                case "name":
                    countryList = LocalityAPI.searchCountryByName(keyword, page, pageSize);
                    break;
                case "code":
                    countryList = LocalityAPI.searchCountryByCode(keyword, page, pageSize);
                    break;
                case "region":
                    countryList = LocalityAPI.searchCountryByRegion(keyword, page, pageSize);
                    break;
                default:
                    return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid search type: %s", searchType));
            }

            List<JsonNode> result = new ArrayList<>();
            for (Country c : countryList)
                result.add(c.toJson());
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
