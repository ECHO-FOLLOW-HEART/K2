package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.geo.Country;
import models.geo.Locality;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.taozi.geo.LocalityFormatter;
import utils.formatter.taozi.geo.SimpleCountryFormatter;
import utils.formatter.taozi.geo.SimpleLocalityFormatter;
import utils.formatter.taozi.user.DetailedPOIFormatter;
import utils.formatter.taozi.user.SimplePOIFormatter;

import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * 地理相关
 * <p/>
 * Created by zephyre on 14-6-20.
 */
public class GeoCtrl extends Controller {
    /**
     * 根据id查看城市详情
     *
     * @param id
     * @return
     */
    public static Result getLocality(String id) {
        try {
            Locality locality = GeoAPI.locDetails(id);
            //JsonNode response = locality.toJson(3);
            JsonNode response = new LocalityFormatter().format(locality);
            return Utils.createResponse(ErrorCode.NORMAL, response);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 通过关键词搜索地理信息。
     *
     * @param searchWord 搜索关键词。
     * @param prefix     是否为前缀搜索。
     */
    public static Result searchGeo(String searchWord, boolean prefix, int page, int pageSize) {
        searchWord = (searchWord != null ? searchWord.trim() : "");
        List<JsonNode> cityList = new ArrayList<>();
        List<JsonNode> countryList;
        try {
            countryList = GeoAPI.searchCountry(searchWord, page, pageSize);
            for (Iterator<Locality> it =
                         GeoAPI.searchLocalities(searchWord, prefix, null, page, pageSize);
                 it.hasNext(); )
                cityList.add(new LocalityFormatter().format(it.next()));
        } catch (PatternSyntaxException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "KeyWord Pattern Error.");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        List<List<JsonNode>> results = new ArrayList<>();
        results.add(cityList);
        results.add(countryList);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }

    /**
     * 发现接口
     *
     * @param loc
     * @param vs
     * @param hotel
     * @param restaurant
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explore(boolean details, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                 boolean country, int page, int pageSize) throws TravelPiException {
        ObjectNode results = Json.newObject();

        // 发现城市
        try {
            if (loc) {
                List<JsonNode> retLocList = new ArrayList<>();
                //获得城市信息
                // TODO 暂时只返回国内数据
                List<Locality> localityList = LocalityAPI.explore(details, false, page, pageSize);
                for (Locality locality : localityList)
                    retLocList.add(new LocalityFormatter().format(locality));
                results.put("loc", Json.toJson(retLocList));
            }

            //发现poi
            HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
            if (vs)
                poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");

            if (hotel)
                poiMap.put(PoiAPI.POIType.HOTEL, "hotel");

            if (restaurant)
                poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");

            for (Map.Entry<PoiAPI.POIType, String> entry : poiMap.entrySet()) {
                List<JsonNode> retPoiList = new ArrayList<>();
                PoiAPI.POIType poiType = entry.getKey();
                String poiTypeName = entry.getValue();

                // TODO 暂时返回国内数据
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, null, false, page, pageSize);
                     it.hasNext(); )
                    retPoiList.add(new DetailedPOIFormatter().format(it.next()));
                results.put(poiTypeName, Json.toJson(retPoiList));
            }

            //发现国家
            if (country) {
                List<JsonNode> retcountryList = new ArrayList<>();
                //获得城市信息
                for (Country tmpCountry : LocalityAPI.exploreCountry(page, pageSize))
                    retcountryList.add(new SimpleCountryFormatter().format(tmpCountry));
                results.put("country", Json.toJson(retcountryList));
            }
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
        } catch (NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }

    }


    /**
     * 返回国内的城市信息
     *
     * @return
     */
    public static Result getLocalities() {
        try {
            List<JsonNode> result = new ArrayList<>();
            List<Locality> localityList = GeoAPI.getLocalities();
            for (Locality locality : localityList) {
                result.add(new SimpleLocalityFormatter().format(locality));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 特定地点美食、景点、购物发现
     * @param locId
     * @param vs
     * @param hotel
     * @param restaurant
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePOI(String locId, boolean vs, boolean hotel, boolean restaurant,
                                    int page, int pageSize) {
        try {
            ObjectNode results = Json.newObject();
            HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
            if (vs)
                poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");

            if (hotel)
                poiMap.put(PoiAPI.POIType.HOTEL, "hotel");

            if (restaurant)
                poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");

            for (Map.Entry<PoiAPI.POIType, String> entry : poiMap.entrySet()) {
                List<JsonNode> retPoiList = new ArrayList<>();
                PoiAPI.POIType poiType = entry.getKey();
                String poiTypeName = entry.getValue();

                // TODO 暂时返回国内数据
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, new ObjectId(locId), false, page, pageSize);
                     it.hasNext(); )
                    retPoiList.add(new SimplePOIFormatter().format(it.next()));
                results.put(poiTypeName, Json.toJson(retPoiList));
            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}
