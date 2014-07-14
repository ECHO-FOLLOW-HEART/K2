package core;

import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Locality相关的核心接口。
 *
 * @author Zephyre
 */
public class LocalityAPI {

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @param level 查询级别。
     * @return      如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static Locality locDetails(String locId, int level) throws TravelPiException {
        try {
            return locDetails(new ObjectId(locId), level);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
        }
    }

    /**
     * 通过百度ID获得城市详情。
     *
     * @param baiduId
     * @return
     * @throws TravelPiException
     */
    public static Locality locDetailsByBaiduId(int baiduId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        return ds.createQuery(Locality.class).field("baiduId").equal(baiduId).get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return      如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static Locality locDetails(ObjectId locId, int level) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(locId);

        List<String> fields = new ArrayList<>();
//        fields.addAll(ArrayLists (new String[]{"name"}).)
        query.retrievedFields(true, "zhName", "parent", "level");
        if (level > 1)
            query.retrievedFields(true, "desc", "imageList", "tags", "coords");
        return query.get();
    }


    /**
     * 通过关键词对城市进行搜索。
     *
     * @param keyword      搜索关键词
     * @param prefix    是否为前缀搜索？
     * @param page
     * @param pageSize
     * @return
     */
    public static List<Locality> searchLocalities(String keyword, boolean prefix, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Pattern pattern;
        if (prefix)
            pattern= Pattern.compile("^"+keyword);
        else
            pattern=Pattern.compile(keyword);
        Query<Locality> query = ds.createQuery(Locality.class).filter("zhName", pattern);
        return query.offset(page * pageSize).limit(pageSize).asList();
    }

    /**
     * 发现城市。
     *
     * @param showDetails   是否显示详情。
     * @param page          分页。
     * @param pageSize      页面大小。
     * @return
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


//    /**
//     * 获得Json格式的城市详情。
//     *
//     * @param node
//     * @param level
//     * @return
//     */
//    public static ObjectNode getLocDetailsJson(DBObject node, int level) throws TravelPiException {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
//                .add("_id", node.get("_id").toString())
//                .add("name", node.get("zhName"));
//
//        Object tmp;
//        if (level >= 2) {
//            tmp = node.get("tags");
//            builder.add("tags", ((tmp == null || !(tmp instanceof BasicDBList)) ? new BasicDBList() : tmp));
//            tmp = node.get("imageList");
//            if (tmp == null || !(tmp instanceof BasicDBList))
//                builder.add("imageList", new BasicDBList());
//            else {
//                BasicDBList imageList = new BasicDBList();
//                for (Object tmp1 : (BasicDBList) tmp)
//                    imageList.add(tmp1.toString());
//                builder.add("imageList", imageList);
//            }
//
//            if (level >= 3) {
//                tmp = node.get("travelMonth");
//                builder.add("travelMonth", ((tmp == null || !(tmp instanceof BasicDBList)) ? new BasicDBList() : tmp));
//                tmp = node.get("country");
//                builder.add("country", (tmp == null ? "" : tmp));
//                tmp = node.get("parent");
//
//
//                if (tmp == null)
//                    builder.add("parent", new BasicDBList());
//                else
//                    builder.add("parent", getLocDetailsJson(locDetails(((DBObject) tmp).get("_id").toString()), 1));
//
//                tmp = node.get("level");
//                builder.add("level", ((tmp == null || !(tmp instanceof Integer)) ? 0 : tmp));
//                tmp = node.get("siblings");
//                if (tmp == null || !(tmp instanceof BasicDBList))
//                    builder.add("siblings", new BasicDBList());
//                else {
//                    BasicDBList siblings = new BasicDBList();
//                    for (Object tmp1 : (BasicDBList) tmp)
//                        siblings.add(getLocDetailsJson(locDetails(((DBObject) tmp1).get("_id").toString()), 1));
//                    builder.add("siblings", siblings);
//                }
//                tmp = node.get("provCap");
//                builder.add("provCap", ((tmp == null || !(tmp instanceof Boolean)) ? "" : (Boolean) tmp));
//
//                for (String coord : new String[]{"lat", "lng", "blat", "blng"}) {
//                    tmp = node.get(coord);
//                    Double val = null;
//                    if (tmp != null && (tmp instanceof Double))
//                        val = (Double) tmp;
//                    builder.add(coord, (val == null ? "" : val));
//                }
//
//                tmp = node.get("ratings");
//                if (tmp == null || !(tmp instanceof DBObject))
//                    tmp = new BasicDBObject();
//                DBObject ratings = (DBObject) tmp;
//                BasicDBObject ratingsNode = new BasicDBObject();
//                for (String k : new String[]{"shoppingIndex", "dinningIndex"}) {
//                    tmp = ratings.get(k);
//                    if (tmp == null || !(tmp instanceof Integer))
//                        ratingsNode.put(k, "");
//                    else
//                        ratingsNode.put(k, tmp);
//                }
//                builder.add("ratings", ratingsNode);
//
//                for (String k : new String[]{"voteCnt", "favorCnt"}) {
//                    tmp = node.get(k);
//                    if (tmp == null || !(tmp instanceof Integer))
//                        builder.add(k, "");
//                    else
//                        builder.add(k, tmp);
//                }
//            }
//        }
//
//        tmp = node.get("desc");
//        if (level == 2)
//            builder.add("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), Constants.ABBREVIATE_LEN)));
//        else if (level == 3)
//            builder.add("desc", (tmp == null ? "" : tmp.toString()));
//
//        return (ObjectNode) Json.toJson(builder.get());
//    }
}
