package core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import utils.Constants;
import utils.Utils;

import java.net.UnknownHostException;

/**
 * Locality相关的核心接口。
 *
 * @author Zephyre
 */
public class Locality {

    /**
     * 获得城市详情。
     *
     * @param locId
     * @return
     * @throws TravelPiException
     */
    public static DBObject locDetails(String locId) throws TravelPiException {
        try {
            return locDetails(new ObjectId(locId));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
        }
    }

    /**
     * 获得城市详情。
     *
     * @param locId
     * @return
     * @throws TravelPiException
     */
    public static DBObject locDetails(ObjectId locId) throws TravelPiException {
        DBObject node = Utils.getMongoClient().getDB("geo").getCollection("locality").findOne(QueryBuilder.start("_id").is(locId).get());

        if (node == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Cannot find locality: %s.", locId.toString()));

        return node;
    }


    /**
     * 发现城市。
     *
     * @param showDetails 是否显示详情
     * @param page
     * @param pageSize
     * @return
     * @throws UnknownHostException
     */
    public static BasicDBList explore(boolean showDetails, int page, int pageSize) throws TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality");

        BasicDBObjectBuilder facetBuilder = BasicDBObjectBuilder.start("zhName", 1).add("ratings.score", 1);
        if (showDetails)
            facetBuilder.add("imageList", 1).add("tags", 1).add("desc", 1);

        DBCursor cursor = col.find(QueryBuilder.start("level").is(2).get(),
                facetBuilder.get()).skip(page * pageSize).limit(pageSize)
                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get());

        BasicDBList results = new BasicDBList();
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            results.add(loc);
        }

        return results;
    }


    /**
     * 获得Json格式的城市详情。
     *
     * @param node
     * @param level
     * @return
     */
    public static ObjectNode getLocDetailsJson(DBObject node, int level) throws TravelPiException {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
                .add("_id", node.get("_id").toString())
                .add("name", node.get("zhName"));

        Object tmp;
        if (level >= 2) {
            tmp = node.get("tags");
            builder.add("tags", ((tmp == null || !(tmp instanceof BasicDBList)) ? new BasicDBList() : tmp));
            tmp = node.get("imageList");
            if (tmp == null || !(tmp instanceof BasicDBList))
                builder.add("imageList", new BasicDBList());
            else {
                BasicDBList imageList = new BasicDBList();
                for (Object tmp1 : (BasicDBList) tmp)
                    imageList.add(tmp1.toString());
                builder.add("imageList", imageList);
            }

            if (level >= 3) {
                tmp = node.get("travelMonth");
                builder.add("travelMonth", ((tmp == null || !(tmp instanceof BasicDBList)) ? new BasicDBList() : tmp));
                tmp = node.get("country");
                builder.add("country", (tmp == null ? "" : tmp));
                tmp = node.get("parent");


                if (tmp == null)
                    builder.add("parent", new BasicDBList());
                else
                    builder.add("parent", getLocDetailsJson(locDetails(((DBObject) tmp).get("_id").toString()), 1));

                tmp = node.get("level");
                builder.add("level", ((tmp == null || !(tmp instanceof Integer)) ? 0 : tmp));
                tmp = node.get("siblings");
                if (tmp == null || !(tmp instanceof BasicDBList))
                    builder.add("siblings", new BasicDBList());
                else {
                    BasicDBList siblings = new BasicDBList();
                    for (Object tmp1 : (BasicDBList) tmp)
                        siblings.add(getLocDetailsJson(locDetails(((DBObject) tmp1).get("_id").toString()), 1));
                    builder.add("siblings", siblings);
                }
                tmp = node.get("provCap");
                builder.add("provCap", ((tmp == null || !(tmp instanceof Boolean)) ? "" : (Boolean) tmp));

                for (String coord : new String[]{"lat", "lng", "blat", "blng"}) {
                    tmp = node.get(coord);
                    Double val = null;
                    if (tmp != null && (tmp instanceof Double))
                        val = (Double) tmp;
                    builder.add(coord, (val == null ? "" : val));
                }

                tmp = node.get("ratings");
                if (tmp == null || !(tmp instanceof DBObject))
                    tmp = new BasicDBObject();
                DBObject ratings = (DBObject) tmp;
                BasicDBObject ratingsNode = new BasicDBObject();
                for (String k : new String[]{"shoppingIndex", "dinningIndex"}) {
                    tmp = ratings.get(k);
                    if (tmp == null || !(tmp instanceof Integer))
                        ratingsNode.put(k, "");
                    else
                        ratingsNode.put(k, tmp);
                }
                builder.add("ratings", ratingsNode);

                for (String k : new String[]{"voteCnt", "favorCnt"}) {
                    tmp = node.get(k);
                    if (tmp == null || !(tmp instanceof Integer))
                        builder.add(k, "");
                    else
                        builder.add(k, tmp);
                }
            }
        }

        tmp = node.get("desc");
        if (level == 2)
            builder.add("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), Constants.ABBREVIATE_LEN)));
        else if (level == 3)
            builder.add("desc", (tmp == null ? "" : tmp.toString()));

        return (ObjectNode) Json.toJson(builder.get());
    }
}
