package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import utils.Constants;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * POI相关核心接口。
 *
 * @author Zephyre
 */
public class PoiAPI {

    public enum POIType {
        VIEW_SPOT,
        HOTEL,
        RESTAURANT,
        SHOPPING,
        ENTERTAINMENT
    }

    /**
     * POI搜索。
     *
     * @param poiType    POI类型。
     * @param locId      POI所在地区（可以为NULL）。
     * @param searchWord 搜索关键词（可以为NULL）。
     * @param details    是否获取详情。
     * @param sortField
     * @param asc
     * @param page
     * @param pageSize
     * @return
     */
    public static DBObject poiSearch(POIType poiType, String locId, String searchWord, boolean details,
                                     String sortField, boolean asc, int page, int pageSize) throws TravelPiException {
        String colName;
        switch (poiType) {
            case VIEW_SPOT:
                colName = "view_spot";
                break;
            case HOTEL:
                colName = "hotel";
                break;
            case RESTAURANT:
                colName = "restaurant";
                break;
            default:
                throw new TravelPiException(ErrorCode.UNSUPPORTED_OP, "Unsupported POI type.");
        }

        QueryBuilder query = QueryBuilder.start();
        if (locId != null) {
            try {
                query.and("geo.locId").is(new ObjectId(locId)).get();
            } catch (IllegalArgumentException e) {
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
            }
        }
        if (searchWord != null)
            query.and("name").regex(Pattern.compile(searchWord));

        BasicDBObjectBuilder facet = BasicDBObjectBuilder.start();
        if (!details)
            facet.add("imageList", 1).add("tags", 1).add("desc", 1).add("geo.locId", 1).add("geo.locName", 1);

        BasicDBObjectBuilder sortBuilder = BasicDBObjectBuilder.start();
        switch (sortField) {
            case "score":
                sortBuilder.add("ratings.score", (asc ? 1 : -1));
                break;
        }
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
        DBCursor cursor = col.find(query.get()).skip(page * pageSize).limit(pageSize);
        cursor.sort(sortBuilder.get());

        BasicDBList poiList = new BasicDBList();
        while (cursor.hasNext())
            poiList.add(cursor.next());

        return poiList;
    }

    /**
     * 获得POI信息。
     *
     * @param poiId
     * @param poiType
     * @param showDetails 是否返回详情。
     * @return
     */
    public static DBObject getPOIInfo(String poiId, POIType poiType, boolean showDetails) throws TravelPiException {
        String colName;
        switch (poiType) {
            case VIEW_SPOT:
                colName = "view_spot";
                break;
            case HOTEL:
                colName = "hotel";
                break;
            case RESTAURANT:
                colName = "restaurant";
                break;
            default:
                throw new TravelPiException(ErrorCode.UNSUPPORTED_OP, "Unsupported POI type.");
        }

        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
        try {
            DBObject query = QueryBuilder.start("_id").is(new ObjectId(poiId)).get();
            DBObject poi;
            if (showDetails)
                poi = col.findOne(query);
            else
                poi = col.findOne(query, BasicDBObjectBuilder.start()
                        .add("name", 1).add("imageList", 1).add("tags", 1).add("desc", 1)
                        .add("geo", 1).get());
            if (poi == null)
                throw new NullPointerException();
            return poi;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", poiId));
        }
    }

    /**
     * 发现POI。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static BasicDBList explore(boolean showDetails, POIType poiType, String locId, int page, int pageSize) throws TravelPiException {
        String colName;
        switch (poiType) {
            case VIEW_SPOT:
                colName = "view_spot";
                break;
            case HOTEL:
                colName = "hotel";
                break;
            case RESTAURANT:
                colName = "restaurant";
                break;
            default:
                throw new TravelPiException(ErrorCode.UNSUPPORTED_OP, "Unsupported POI type.");
        }

        ObjectId locOID = null;
        if (locId != null) {
            try {
                locOID = new ObjectId(locId);
            } catch (IllegalArgumentException e) {
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s", locId));
            }
        }
        QueryBuilder queryBuilder = QueryBuilder.start();
        if (locOID != null)
            queryBuilder.and("geo.locId").is(locOID);

        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
        BasicDBObjectBuilder facetBuilder = BasicDBObjectBuilder.start("name", 1).add("geo.locId", 1).add("geo.locName", 1);
        if (showDetails)
            facetBuilder.add("imageList", 1).add("ratings.score", 1).add("tags", 1).add("desc", 1);

        DBCursor cursor = col.find(queryBuilder.get(), facetBuilder.get()).skip(page * pageSize)
                .limit(pageSize).sort(BasicDBObjectBuilder.start("ratings.score", -1).get());

        BasicDBList results = new BasicDBList();
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            results.add(loc);

        }

        return results;
    }


    public static List<JsonNode> getSuggestions(POIType poiType, String word, int page, int pageSize) throws TravelPiException {
        String colName;
        switch (poiType) {
            case VIEW_SPOT:
                colName = "view_spot";
                break;
            case HOTEL:
                colName = "hotel";
                break;
            case RESTAURANT:
                colName = "restaurant";
                break;
            default:
                return new ArrayList<>();
        }

        Pattern pattern = Pattern.compile("^" + word);
        DBCollection colLoc;
        try {
            colLoc = Utils.getMongoClient().getDB("poi").getCollection(colName);
        } catch (TravelPiException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, e.getMessage(), e);
        }

        DBObject qb = QueryBuilder.start("name").regex(pattern).get();
        DBCursor cursor = colLoc.find(qb, BasicDBObjectBuilder.start("name", 1).add("geo", 1).get())
                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get()).limit(pageSize);
        List<JsonNode> results = new ArrayList<>();
        while (cursor.hasNext())
            results.add(getPOIInfoJson(cursor.next(), 1));
        return results;
    }


    /**
     * 获得Json格式的POI信息。
     *
     * @param node
     * @param level
     * @return
     */
    public static ObjectNode getPOIInfoJson(DBObject node, int level) {
        DBObject retVs = new BasicDBObject();

        retVs.put("_id", node.get("_id").toString());
        retVs.put("name", node.get("name").toString());
        Object tmp;

        tmp = node.get("geo");
        if (tmp == null || !(tmp instanceof DBObject))
            tmp = new BasicDBObject();
        DBObject geoNode = (DBObject) tmp;
        BasicDBObjectBuilder geoBuilder = BasicDBObjectBuilder.start();
        geoBuilder.add("locId", geoNode.get("locId").toString()).add("locName", geoNode.get("locName"));
        if (level >= 3) {
            for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                tmp = geoNode.get(k);
                if (tmp != null && (tmp instanceof Double))
                    geoBuilder.add(k, tmp);
                else
                    geoBuilder.add(k, "");
            }
        }
        retVs.put("geo", geoBuilder.get());

        if (level >= 2) {
            BasicDBList retTagList = new BasicDBList();
            tmp = node.get("tags");
            if (tmp == null || !(tmp instanceof BasicDBList))
                tmp = new BasicDBList();
            for (Object tmp2 : (BasicDBList) tmp)
                retTagList.add(tmp2.toString());
            retVs.put("tags", retTagList);

            BasicDBList retImageList = new BasicDBList();
            tmp = node.get("imageList");
            if (tmp == null || !(tmp instanceof BasicDBList))
                tmp = new BasicDBList();
            for (Object tmp2 : (BasicDBList) tmp)
                retImageList.add(tmp2.toString());
            retVs.put("imageList", retImageList);

            if (level >= 3) {
                tmp = node.get("price");
                retVs.put("cost", ((tmp == null || !(tmp instanceof Double)) ? "" : (double) tmp));
                tmp = node.get("priceDesc");
                retVs.put("costDesc", (tmp == null ? "" : tmp.toString()));
                retVs.put("timeCost", "");
            }
        }

        tmp = node.get("desc");
        if (level == 2)
            retVs.put("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), Constants.ABBREVIATE_LEN)));
        else if (level >= 3)
            retVs.put("desc", (tmp == null ? "" : tmp.toString()));

        return (ObjectNode) Json.toJson(retVs);
    }
}
