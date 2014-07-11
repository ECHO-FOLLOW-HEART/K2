package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.LocalityAPI;
import core.PoiAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.TravelPiBaseItem;
import models.geos.Locality;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


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
     * @param page
     * @param pageSize
     * @return
     */
    public static Result searchLocality(String searchWord, int page, int pageSize) throws UnknownHostException, TravelPiException {


        if (searchWord == null || searchWord.trim().isEmpty())
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid search word: %s.", searchWord));

        MongoClient client = Utils.getMongoClient();
        DB db = client.getDB("geo");
        DBCollection col = db.getCollection("locality");

        QueryBuilder qb = new QueryBuilder();
        qb.or(QueryBuilder.start("zhName").regex(Pattern.compile("^" + searchWord)).get(),
                QueryBuilder.start("alias").regex(Pattern.compile("^" + searchWord)).get());

        BasicDBObjectBuilder fields = new BasicDBObjectBuilder();
        fields.add("zhName", 1).add("level", 1).add("baiduId", 1).add("distId", 1).add("lat", 1).add("lng", 1)
                .add("desc", 1).add("siblings", 1).add("parent", 1);
        DBCursor cursor = col.find(qb.get(), fields.get()).skip(page * pageSize).limit(pageSize);

        List<JsonNode> results = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            loc.put("_id", loc.get("_id").toString());

            BasicDBList siblings;
            if ((siblings = (BasicDBList) loc.get("siblings")) != null) {
                for (Object sibling : siblings) {
                    DBObject sib = (DBObject) sibling;
                    sib.put("_id", sib.get("_id").toString());
                }
            }
            DBObject p;
            if ((p = (DBObject) loc.get("parent")) != null) {
                ObjectId pid = (ObjectId) p.get("_id");
                p.put("_id", pid.toString());
            }
            loc.put("_id", loc.get("_id").toString());

            JsonNode jsonItem = Json.parse(loc.toString());
            results.add(jsonItem);
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    /**
     * 获得城市信息。
     *
     * @param id
     * @return
     */
    public static Result getLocality(String id, int relatedVs, int hotel, int restaurant) {

        try {
//            ObjectNode result = LocalityAPI.getLocDetailsJson(LocalityAPI.locDetails(id), 3);
            ObjectNode result = (ObjectNode) LocalityAPI.locDetails(id, 3).toJson(3);

            int page = 0;
            int pageSize = 10;
            if (relatedVs != 0) {
                BasicDBList retVsList = new BasicDBList();
                for (Object tmp1 : PoiAPI.explore(true, PoiAPI.POIType.VIEW_SPOT, id, page, pageSize)) {
                    ObjectNode vs = PoiAPI.getPOIInfoJson((DBObject) tmp1, 1);
                    retVsList.add(vs);
                }
                result.put("relatedVs", Json.toJson(retVsList));
            } else
                result.put("relatedVs", Json.toJson(new ArrayList<>()));

            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result lookupLocality(int baiduId) throws TravelPiException {
        TravelPiBaseItem loc = LocalityAPI.locDetailsByBaiduId(baiduId);
        if (loc == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid Baidu ID: %d.", baiduId));
        else
            return Utils.createResponse(ErrorCode.NORMAL, loc.toJson());
    }
}
