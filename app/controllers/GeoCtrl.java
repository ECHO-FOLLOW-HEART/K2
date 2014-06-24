package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.geos.Locality;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * 地理相关
 * <p>
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

    public static Result getLocality(long id) {
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

    public static Result searchLocality(String searchWord, int page, int pageSize) {
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
}
