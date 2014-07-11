package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import core.PoiAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * POI相关信息。
 *
 * @author Zephyre
 */
public class POICtrl extends Controller {

    /**
     * 获得景点的详细信息。
     *
     * @param spotId      景点ID。
     * @param showDetails 获得更多的详情。
     * @param showRelated 获得相关景点。
     */
    public static Result viewSpotInfo(String spotId, int showDetails, int showRelated) throws TravelPiException {
        boolean details = (showDetails != 0);
        DBObject poiInfo = PoiAPI.getPOIInfo(spotId, PoiAPI.POIType.VIEW_SPOT, details);
        ObjectNode results = PoiAPI.getPOIInfoJson(poiInfo, (details ? 3 : 2));

        if (showRelated != 0) {
            // 获得相关景点
            List<JsonNode> related = new ArrayList<>();
            int page = 0;
            int pageSize = 10;
            String locId = results.get("geo").get("locId").asText();
            for (Object tmp : PoiAPI.explore(true, PoiAPI.POIType.VIEW_SPOT, locId, page, pageSize))
                related.add(PoiAPI.getPOIInfoJson((DBObject) tmp, 2));
            results.put("related", Json.toJson(related));
        }

        return Utils.createResponse(ErrorCode.NORMAL, results);

//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        builder.add("_id", poiInfo.get("_id").toString());
//        builder.add("name", poiInfo.get("name").toString());
//
//        Object tmp;
//        tmp = poiInfo.get("desc");
//        if (detailsFlag)
//            builder.add("desc", (tmp == null ? "" : tmp.toString()));
//        else
//            builder.add("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), 64)));
//
//        for (String k : new String[]{"tags", "imageList"}) {
//            tmp = poiInfo.get(k);
//            if (tmp == null || !(tmp instanceof BasicDBList))
//                tmp = new BasicDBList();
//            BasicDBList valList = new BasicDBList();
//            for (Object tmp1 : (BasicDBList) tmp)
//                valList.add(tmp1.toString());
//            builder.add(k, valList);
//        }
//
//        if (detailsFlag) {
//            for (String k : new String[]{"voteCnt", "favorCnt"}) {
//                tmp = poiInfo.get(k);
//                if (tmp == null || !(tmp instanceof Integer))
//                    builder.add(k, "");
//                else
//                    builder.add(k, tmp);
//            }
//
////            tmp = poiInfo.get("ratings");
////            if (tmp == null || !(tmp instanceof DBObject))
////                tmp = new BasicDBObject();
////            DBObject ratings = (DBObject) tmp;
////            DBObject retRatings = new BasicDBObject();
////            tmp = ratings.get("voteCnt");
////            retRatings.put("voteCnt", ((tmp == null || !(tmp instanceof Integer)) ? "" : (int) tmp));
////            builder.add("ratings", retRatings);
//
//            tmp = poiInfo.get("price");
//            builder.add("cost", ((tmp == null || !(tmp instanceof Double)) ? "" : (double) tmp));
//            tmp = poiInfo.get("priceDesc");
//            builder.add("costDesc", (tmp == null ? "" : tmp.toString()));
//
//            tmp = poiInfo.get("geo");
//            if (tmp == null || !(tmp instanceof DBObject))
//                tmp = new BasicDBObject();
//            DBObject geo = (DBObject) tmp;
//            DBObject retGeo = new BasicDBObject();
//            for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
//                tmp = geo.get(k);
//                if (tmp != null && (tmp instanceof Double))
//                    retGeo.put(k, tmp);
//                else
//                    retGeo.put(k, "");
//            }
//            for (String k : new String[]{"locId", "locName"}) {
//                tmp = geo.get(k);
//                retGeo.put(k, (tmp == null ? "" : tmp.toString()));
//            }
//
//            builder.add("geo", retGeo);
//        }
//
//        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.get()));


//        ObjectNode result = Json.newObject();
//        result.put("_id", poiInfo.get("_id").toString());
//        poiInfo.put("name", poiInfo.get("name").toString());
//
//        if (showDetails != 0) {
//            DBObject ratings = (DBObject) poiInfo.get("ratings");
//            if (ratings == null)
//                ratings = new BasicDBObject();
//            ObjectNode ratingsJ = Json.newObject();
//            for (String k : new String[]{"foodIndex", "shoppingIndex", "score"}) {
//                Object tmp = ratings.get(k);
//                if (tmp == null)
//                    ratingsJ.put(k, "");
//                else
//                    ratingsJ.put(k, Json.toJson(tmp));
//            }
//            result.put("ratings", ratingsJ);
//        }

        // 获得关联信息
        // 返回该城市中的景点
//        if (showRelated != 0) {
//            try {
//                ObjectId locId = (ObjectId) ((DBObject) poiInfo.get("geo")).get("locId");
//                int page = 0;
//                int pageSize = 10;
//                BasicDBList spotList = POI.exploreViewSpots(true, locId.toString(), page, pageSize);
//
//                if (spotList != null && !spotList.isEmpty())
//                    poiInfo.put("related", spotList);
//            } catch (NullPointerException ignored) {
//            }
//        }

//        ObjectNode node = (ObjectNode) Utils.bsonToJson(poiInfo);
//        node.put("api", result);

//        return Utils.createResponse(ErrorCode.NORMAL, node);
    }


    public static Result viewSpotList(String locality, String tagFilter, String sortFilter, String sort,
                                      int page, int pageSize) throws UnknownHostException, TravelPiException {
        return poiList("vs", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result hotelList(String locality, String tagFilter, String sortFilter, String sort,
                                   int page, int pageSize) throws UnknownHostException, TravelPiException {
        return poiList("hotel", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    public static Result restaurantList(String locality, String tagFilter, String sortFilter, String sort,
                                        int page, int pageSize) throws UnknownHostException, TravelPiException {
        return poiList("restaurant", locality, tagFilter, sortFilter, sort, page, pageSize);
    }

    /**
     * 搜索景点的ID。
     *
     * @param locality   景点所在地区的ID。
     * @param tagFilter  按照标签对景点进行过滤。
     * @param sortFilter 景点列表排序的字段，目前仅支持rating，即按照评分进行排序。
     * @param sort       升序(asc)还是降序(desc)
     * @param page       起始页码。
     * @param pageSize   页面大小。
     */
    private static Result poiList(String poiType, String locality, String tagFilter, String sortFilter, String sort,
                                  int page, int pageSize) throws UnknownHostException, TravelPiException {
        String colName = null;
        switch (poiType) {
            case "vs":
                colName = "view_spot";
                break;
            case "hotel":
                colName = "hotel";
                break;
            case "restaurant":
                colName = "restaurant";
                break;
        }
        if (colName == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);

        BasicDBObject query = new BasicDBObject();
        try {
            query.put("geo.locality._id", new ObjectId(locality));
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locality));
        }
        if (tagFilter != null && !tagFilter.isEmpty())
            query.put("tags", tagFilter);
        DBCursor cursor = col.find(query).skip(page * pageSize).limit(pageSize);
        if (sort.equals("asc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", 1));
        else if (sort.equals("desc"))
            cursor = cursor.sort(new BasicDBObject("ratings.score", -1));

        List<JsonNode> resultList = new ArrayList<>();
        while (cursor.hasNext()) {
            DBObject item = cursor.next();
            ObjectNode node = Json.newObject();
            node.put("_id", item.get("_id").toString());

            DBObject tmp = (DBObject) item.get("ratings");
            if (tmp != null)
                node.put("ratings", Json.toJson(tmp));
            tmp = (DBObject) item.get("tags");
            if (tmp != null)
                node.put("tags", Json.toJson(tmp));

            String name = (String) item.get("name");
            if (name != null)
                node.put("name", name);

            try {
                String desc = (String) ((DBObject) item.get("intro")).get("desc");
                if (desc != null) {
                    desc = StringUtils.abbreviate(desc, 64);
                    node.put("desc", desc);
                }
            } catch (NullPointerException ignore) {
            }

            tmp = (DBObject) item.get("geo");
            if (tmp != null) {
                DBObject loc = (DBObject) tmp.get("locality");
                String locId = loc.get("_id").toString();
                loc.put("_id", locId);
                node.put("geo", Json.toJson(tmp));
            }

            tmp = (DBObject) item.get("imageList");
            if (tmp != null)
                node.put("imageList", Json.toJson(tmp));

            resultList.add(node);
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(resultList));
    }

    /**
     * 获得相关景点。
     *
     * @param spotId
     * @param tagFilter
     * @param sortFilter
     * @param sort
     * @param page
     * @param pageSize
     * @return
     * @throws UnknownHostException
     */
    public static Result relatedViewSpotList(String spotId, String tagFilter, String sortFilter, String sort, int page, int pageSize) throws UnknownHostException, TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection("view_spot");
        String locId = null;
        try {
            DBObject vs = col.findOne(QueryBuilder.start("_id").is(new ObjectId(spotId)).get(),
                    BasicDBObjectBuilder.start("geo.locality._id", 1).get());
            locId = ((DBObject) ((DBObject) vs.get("geo")).get("locality")).get("_id").toString();
            if (locId == null)
                throw new NullPointerException();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid view spot ID: %s.", spotId));
        }

        return viewSpotList(locId, tagFilter, sortFilter, sort, page, pageSize);
    }
}
