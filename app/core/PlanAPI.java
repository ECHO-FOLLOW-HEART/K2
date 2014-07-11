package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import org.bson.types.ObjectId;
import play.libs.Json;
import utils.Utils;

/**
 * 路线规划相关API。
 *
 * @author Zephyre
 */
public class PlanAPI {

    /**
     * 发现路线
     *
     * @param locId
     * @param poiId
     * @param sort
     * @param tags
     * @param page
     * @param pageSize
     * @param sortField @return
     * @throws TravelPiException
     */
    public static BasicDBList explore(String locId, String poiId, String sort, String tags, int page, int pageSize, String sortField) throws TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
        DBCursor cursor;

        QueryBuilder queryBuilder;
        try {
            queryBuilder = QueryBuilder.start();
            if (locId != null && !locId.isEmpty())
                queryBuilder.and("loc._id").is(new ObjectId(locId));
            if (poiId != null && !poiId.isEmpty())
                queryBuilder.and("details.actv.itemId").is(new ObjectId(poiId));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT,
                    String.format("Invalid locality ID: %s or POI ID: %s.", (locId == null ? "NULL" : locId),
                            (poiId == null ? "NULL" : poiId)));
        }

        if (tags != null && !tags.isEmpty())
            queryBuilder = queryBuilder.and("tags").is(tags);
        int sortVal = 1;
        if (sort != null && (sort.equals("asc") || sort.equals("desc")))
            sortVal = sort.equals("asc") ? 1 : -1;

        BasicDBObjectBuilder facet = BasicDBObjectBuilder.start();
        facet.add("loc", 1).add("viewCnt", 1).add("tags", 1).add("title", 1).add("days", 1).add("imageList", 1).add("desc", 1);

        cursor = col.find(queryBuilder.get(), facet.get());
        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "days":
                    cursor.sort(BasicDBObjectBuilder.start("days", sortVal).get());
                    break;
                case "hot":
                    cursor.sort(BasicDBObjectBuilder.start("viewCnt", sortVal).get());
            }
        }
        cursor.skip(page * pageSize).limit(pageSize);


        BasicDBList results = new BasicDBList();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }

        return results;
    }

    public static DBObject getPlan(String planId) throws TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
        try {
            if (planId == null)
                throw new NullPointerException();
            return col.findOne(QueryBuilder.start("_id").is(new ObjectId(planId)).get());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
        }
    }

    public static JsonNode getPlanJson(DBObject item) throws TravelPiException {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        builder.add("_id", item.get("_id").toString());
        DBObject loc = (DBObject) item.get("loc");

        builder.add("loc", LocalityAPI.locDetails((ObjectId) loc.get("_id"), 1).toJson(1));
//        builder.add("loc", LocalityAPI.getLocDetailsJson(LocalityAPI.locDetails((ObjectId) loc.get("_id")), 1));

        Object tmp;
        tmp = item.get("title");
        builder.add("title", (tmp == null ? "" : tmp.toString()));

        for (String k : new String[]{"days", "viewCnt"}) {
            tmp = item.get(k);
            builder.add(k, (tmp == null || !(tmp instanceof Integer)) ? "" : (int) tmp);
        }

        tmp = item.get("desc");
        builder.add("desc", (tmp == null ? "" : tmp.toString()));

        for (String k : new String[]{"tags", "imageList"}) {
            BasicDBList valList = new BasicDBList();
            tmp = item.get(k);
            if (tmp != null && (tmp instanceof BasicDBList)) {
                for (Object t : (BasicDBList) tmp)
                    valList.add(t.toString());
            }
            builder.add(k, valList);
        }
        return Json.toJson(builder.get());
    }
}
