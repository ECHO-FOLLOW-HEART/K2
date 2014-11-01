package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.LocalityAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.taozi.geo.LocalityFormatter;

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
     * 根据id查看城市详情
     *
     * @param id
     * @return
     */
    public static Result getLocality(String id) {
        try {
            Locality locality = GeoAPI.locDetails(id);
            //JsonNode response = locality.toJson(3);
            JsonNode response=new LocalityFormatter().format(locality);
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
    public static Result searchLocality(String searchWord, int prefix) {
        int page;
        try {
            page = Integer.parseInt(request().getQueryString("page"));
        } catch (NullPointerException | NumberFormatException ignore) {
            page = 0;
        }

        int pageSize;
        try {
            pageSize = Integer.parseInt(request().getQueryString("pageSize"));
        } catch (NullPointerException | NumberFormatException ignore) {
            pageSize = 10;
        }

        searchWord = (searchWord != null ? searchWord.trim() : "");
        List<JsonNode> cityList = new ArrayList<>();
        List<JsonNode> countryList;
        try {
            countryList = GeoAPI.searchCountry(searchWord, page, pageSize);
            for (Iterator<Locality> it =
                         GeoAPI.searchLocalities(searchWord, (prefix != 0), page, pageSize);
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
}