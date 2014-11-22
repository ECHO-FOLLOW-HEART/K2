package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.geo.Country;
import models.geo.Destination;
import models.geo.Locality;
import models.misc.TravelNote;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.Utils;
import utils.formatter.taozi.geo.CountryFormatter;
import utils.formatter.taozi.geo.DestinationFormatter;
import utils.formatter.taozi.geo.LocalityFormatter;
import utils.formatter.taozi.geo.SimpleCountryFormatter;
import utils.formatter.taozi.geo.SimpleLocalityFormatter;
import utils.formatter.taozi.misc.TravelNoteFormatter;
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
     * @param id      城市ID
     * @param noteCnt 游记个数
     * @return
     */
    public static Result getLocality(String id, int noteCnt) {
        try {
            Locality locality = GeoAPI.locDetails(id);
            if (locality == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality not exist.");
            ObjectNode response = (ObjectNode) new LocalityFormatter().format(locality);
            List<TravelNote> tras = TravelNoteAPI.searchNoteByLoc(Arrays.asList(locality.zhName), null, noteCnt);
            List<ObjectNode> objs = new ArrayList<>();
            for (TravelNote tra : tras) {
                objs.add((ObjectNode) new TravelNoteFormatter().format(tra));
            }
            response.put("travelNote", Json.toJson(objs));
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


    public static Result exploreDestinations(boolean abroad, int page, int pageSize) {

        try {

            List<ObjectNode> objs = new ArrayList<>();
            if (abroad) {
                List<Country> countrys = GeoAPI.searchCountryByName("", page, pageSize);
                for (Country des : countrys) {
                    objs.add((ObjectNode) new CountryFormatter().format(des));
                }
            } else {
                List<Destination> destinations = GeoAPI.getDestinations(abroad, page, pageSize);
                for (Destination des : destinations) {
                    objs.add((ObjectNode) new DestinationFormatter().format(des));
                }
            }


            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(objs));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

    }
}
