package aizou.core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import models.poi.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * POI相关核心接口。
 *
 * @author Zephyre
 */
public class PoiAPI {

    /**
     * 获得POI联想列表。
     *
     * @param poiType
     * @param word
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> getSuggestions(POIType poiType, String word, int pageSize) throws TravelPiException {
        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        //名称或别名
        query.or(query.criteria("name").equal(Pattern.compile(word)), query.criteria("alias").equal(Pattern.compile(word)));
        // TODO 暂时不做数据过滤
        //if (poiType == POIType.VIEW_SPOT)
        //    query.field("rankingA").greaterThanOrEq(5);
        //query.field("relPlanCnt").greaterThan(0);
        return query.limit(pageSize).iterator();
    }

    /**
     * POI搜索。
     *
     * @param poiType    POI类型。
     * @param locId      POI所在地区（可以为NULL）。
     * @param tag
     * @param searchWord 搜索关键词（可以为NULL）。       @return
     * @param sortField
     * @param asc
     * @param page
     * @param pageSize
     * @param details    是否获取详情。
     * @param extra      其它过滤方式。参见Morphia filter的介绍。
     */
    public static java.util.Iterator<? extends AbstractPOI> poiSearch(POIType poiType, ObjectId locId, String tag,
                                                                      String searchWord, final SortField sortField, boolean asc,
                                                                      int page, int pageSize, boolean details, Map<String, Object> extra, int hotelType)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);

        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);

        if (locId != null)
            query.or(query.criteria("targets").equal(locId), query.criteria("addr.loc.id").equal(locId));
        if (searchWord != null && !searchWord.isEmpty())
            query = query.filter("name", Pattern.compile(searchWord));
        if (tag != null && !tag.isEmpty())
            query = query.field("tags").equal(tag);
        //酒店类型：空-类型不限 1-星级酒店 2-经济型酒店 3-青年旅社 4-民俗酒店
        if (hotelType != 0)
            query = query.field("type").equal(hotelType);

        int detailLvl = details ? 3 : 2;
        try {
            List fieldList = (List) poiClass.getMethod("getRetrievedFields", int.class).invoke(poiClass, detailLvl);
            query.retrievedFields(true, (String[]) fieldList.toArray(new String[]{""}));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            return null;
        }
//        if (!details)
//            query.retrievedFields(true, AbstractPOI.getRetrievedFields(2).toArray(new String[]{""}));
        if (extra != null) {
            for (Map.Entry<String, Object> entry : extra.entrySet())
                query = query.filter(entry.getKey(), entry.getValue());
        }
        // 排序
        if (sortField != null) {
            String stKey = null;
            switch (sortField) {
                case PRICE:
                    stKey = "price";
                    break;
                case SCORE:
                    stKey = "ratings.score";
                    break;
            }
            query.order(String.format("%s%s", asc ? "" : "-", stKey));
        } else {
            query.order("-ratings.recommended, -ratings.rankingA, -ratings.qtScore, -ratings.baiduScore, -ratings.viewCnt");
        }

        query.offset(page * pageSize).limit(pageSize);

        return query.iterator();
    }

    /**
     * 重载POI搜索
     *
     * @param poiType
     * @param tag
     * @param searchWord
     * @param sortField
     * @param asc
     * @param page
     * @param pageSize
     * @param hotelType
     * @return
     * @throws TravelPiException
     */
    public static java.util.Iterator<? extends AbstractPOI> poiSearch(POIType poiType, String tag,
                                                                      String searchWord, final SortField sortField, boolean asc,
                                                                      int page, int pageSize, Boolean details, int hotelType)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);

        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Restaurant.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);

        if (searchWord != null && !searchWord.isEmpty())
            query = query.filter("name", Pattern.compile(searchWord));
        if (tag != null && !tag.isEmpty())
            query = query.field("tags").equal(tag);
        //酒店类型：空-类型不限 1-星级酒店 2-经济型酒店 3-青年旅社 4-民俗酒店
        if (hotelType != 0)
            query = query.field("type").equal(hotelType);

        int detailLvl = details ? 3 : 2;
        try {
            List fieldList = (List) poiClass.getMethod("getRetrievedFields", int.class).invoke(poiClass, detailLvl);
            query.retrievedFields(true, (String[]) fieldList.toArray(new String[]{""}));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            return null;
        }
        // 排序
        if (sortField != null) {
            String stKey = null;
            switch (sortField) {
                case PRICE:
                    stKey = "price";
                    break;
                case SCORE:
                    stKey = "ratings.score";
                    break;
            }
            query.order(String.format("%s%s", asc ? "" : "-", stKey));
        } else {
            query.order("-ratings.recommended, -ratings.rankingA, -ratings.qtScore, -ratings.baiduScore, -ratings.viewCnt");
        }

        query.offset(page * pageSize).limit(pageSize);

        return query.iterator();
    }

//    /**
//     * POI搜索。
//     *
//     * @param poiType    POI类型。
//     * @param locId      POI所在地区（可以为NULL）。
//     * @param searchWord 搜索关键词（可以为NULL）。
//     * @param details    是否获取详情。
//     * @param sortField
//     * @param asc
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public static BasicDBList poiSearchOld(POIType poiType, String locId, String searchWord, boolean details,
//                                           String sortField, boolean asc, int page, int pageSize) throws TravelPiException {
//        String colName;
//        switch (poiType) {
//            case VIEW_SPOT:
//                colName = "view_spot";
//                break;
//            case HOTEL:
//                colName = "hotel";
//                break;
//            case RESTAURANT:
//                colName = "restaurant";
//                break;
//            default:
//                throw new TravelPiException(ErrorCode.UNSUPPORTED_OP, "Unsupported POI type.");
//        }
//
//        QueryBuilder query = QueryBuilder.start();
//        if (locId != null) {
//            try {
//                query.and("geo.locId").is(new ObjectId(locId)).get();
//            } catch (IllegalArgumentException e) {
//                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
//            }
//        }
//        if (searchWord != null)
//            query.and("name").regex(Pattern.compile(searchWord));
//
//        BasicDBObjectBuilder facet = BasicDBObjectBuilder.start();
//        if (!details)
//            facet.add("imageList", 1).add("tags", 1).add("desc", 1).add("geo.locId", 1).add("geo.locName", 1);
//
//        BasicDBObjectBuilder sortBuilder = BasicDBObjectBuilder.start();
//        if (sortField != null) {
//            switch (sortField) {
//                case "score":
//                    sortBuilder.add("ratings.score", (asc ? 1 : -1));
//                    break;
//            }
//        }
//        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
//        DBCursor cursor = col.find(query.get()).skip(page * pageSize).limit(pageSize);
//        cursor.sort(sortBuilder.get());
//
//        BasicDBList poiList = new BasicDBList();
//        while (cursor.hasNext())
//            poiList.add(cursor.next());
//
//        return poiList;
//    }

    /**
     * 获得POI信息。
     *
     * @see PoiAPI#getPOIInfo(org.bson.types.ObjectId, PoiAPI.POIType, boolean)
     */
    public static AbstractPOI getPOIInfo(String poiId, POIType poiType, boolean showDetails) throws TravelPiException {
        ObjectId id;
        try {
            id = new ObjectId(poiId);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.",
                    poiId != null ? poiId : "NULL"));
        }
        return getPOIInfo(id, poiType, showDetails);
    }

    /**
     * 获得POI信息相关的推荐
     *
     * @param poiId
     * @return
     * @throws TravelPiException
     */
    public static List<POIRmd> getPOIRmd(String poiId, int page, int pageSize) throws TravelPiException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<POIRmd> query = ds.createQuery(POIRmd.class);
        query.field("poiId").equal(id).offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    /**
     * 获得POI信息相关的推荐条数
     *
     * @param poiId
     * @return
     * @throws TravelPiException
     */
    public static long getPOIRmdCount(String poiId) throws TravelPiException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<POIRmd> query = ds.createQuery(POIRmd.class);
        query.field("poiId").equal(id);
        return ds.getCount(query);
    }

    /**
     * 获得POI信息相关的评论
     *
     * @param poiId
     * @return
     * @throws TravelPiException
     */
    public static List<Comment> getPOIComment(String poiId, int page, int pageSize) throws TravelPiException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class);
        query.field("poiId").equal(id).offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    /**
     * 批量获得POI信息相关的评论
     *
     * @param poiId
     * @return
     * @throws TravelPiException
     */
    public static List<Comment> getPOICommentBatch(List<String> poiId, int page, int pageSize) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class);

        List<CriteriaContainerImpl> criList = new ArrayList<>();
        ObjectId oId;
        for (String tempId : poiId) {
            oId = new ObjectId(tempId);
            criList.add(query.criteria("poiId").equal(oId));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        query.offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    /**
     * 获得POI信息相关的评论条数
     *
     * @param poiId
     * @return
     * @throws TravelPiException
     */
    public static long getPOICommentCount(String poiId) throws TravelPiException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class);
        query.field("poiId").equal(id);
        return ds.getCount(query);
    }

    /**
     * 获得地区的poi
     *
     * @param poiType
     * @param locId
     * @param tagFilter
     * @param sortField
     * @param sort
     * @param details
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static Iterator<? extends AbstractPOI> poiList(POIType poiType, ObjectId locId, String tagFilter, final SortField sortField,
                                                          Boolean sort, Boolean details, int page, int pageSize)
            throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Restaurant.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);

        if (locId == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        query = query.field("addr.loc.id").equal(locId);
        //query.or(query.criteria("targets").equal(locId), query.criteria("addr.loc.id").equal(locId));

        if (tagFilter != null && !tagFilter.isEmpty())
            query = query.field("tags").equal(tagFilter);

        int detailLvl = details ? 3 : 2;
        try {
            List fieldList = (List) poiClass.getMethod("getRetrievedFields", int.class).invoke(poiClass, detailLvl);
            query.retrievedFields(true, (String[]) fieldList.toArray(new String[]{""}));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            return null;
        }
        // 排序
        if (sortField != null) {
            String stKey = null;
            switch (sortField) {
                case PRICE:
                    stKey = "price";
                    break;
                case SCORE:
                    stKey = "ratings.score";
                    break;
                case RATING:
                    stKey = "rating";
                    break;
            }
            query.order(String.format("%s%s", sort ? "" : "-", stKey));
        } else {
            query.order("-ratings.recommended, -ratings.rankingA, -ratings.qtScore, -ratings.baiduScore, -ratings.viewCnt");
        }

        query.offset(page * pageSize).limit(pageSize);

        return query.iterator();
    }

    /**
     * 获得POI信息。
     *
     * @param poiId       POI的id。
     * @param poiType     POI的类型。包括：view_spot: 景点；hotel: 酒店；restaurant: 餐厅。
     * @param showDetails 是否返回详情。
     * @return POI详情。如果没有找到，返回null。
     */
    public static AbstractPOI getPOIInfo(ObjectId poiId, POIType poiType, boolean showDetails) throws TravelPiException {
        Class<? extends AbstractPOI> poiClass;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                // TODO
                poiClass = Shopping.class;
                break;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        return ds.createQuery(poiClass).field("_id").equal(poiId).get();
    }

    public static AbstractPOI getPOIInfo(ObjectId poiId, POIType poiType, List<String> fieldList) throws TravelPiException {
        Class<? extends AbstractPOI> poiClass;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Shopping.class;
                break;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        return query.field("_id").equal(poiId).get();
    }

    /**
     * 发现POI。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> explore(POIType poiType, String locId,
                                                          int page, int pageSize) throws TravelPiException {
        try {
            return explore(poiType, new ObjectId(locId), false, page, pageSize);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.",
                    locId != null ? locId : "NULL"));
        }
    }

    /**
     * 发现POI。
     *
     * @param abroad
     * @param page
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> explore(POIType poiType, ObjectId locId,
                                                          boolean abroad, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);

        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case DINNING:
                poiClass = Dinning.class;
                break;
            case SHOPPING:
                poiClass = Shopping.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        if (locId != null)
            query.or(query.criteria("targets").equal(locId), query.criteria("addr.loc.id").equal(locId));

        query.field("abroad").equal(abroad);
        return query.offset(page * pageSize).limit(pageSize).order("-ratings.score").iterator();
    }

//    /**
//     * 发现POI。
//     *
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public static BasicDBList exploreOld(boolean showDetails, POIType poiType, String locId, int page, int pageSize) throws TravelPiException {
//        String colName;
//        switch (poiType) {
//            case VIEW_SPOT:
//                colName = "view_spot";
//                break;
//            case HOTEL:
//                colName = "hotel";
//                break;
//            case RESTAURANT:
//                colName = "restaurant";
//                break;
//            default:
//                throw new TravelPiException(ErrorCode.UNSUPPORTED_OP, "Unsupported POI type.");
//        }
//
//        ObjectId locOID = null;
//        if (locId != null) {
//            try {
//                locOID = new ObjectId(locId);
//            } catch (IllegalArgumentException e) {
//                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s", locId));
//            }
//        }
//        QueryBuilder queryBuilder = QueryBuilder.start();
//        if (locOID != null)
//            queryBuilder.and("geo.locId").is(locOID);
//
//        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection(colName);
//        BasicDBObjectBuilder facetBuilder = BasicDBObjectBuilder.start("name", 1).add("geo.locId", 1).add("geo.locName", 1);
//        if (showDetails)
//            facetBuilder.add("imageList", 1).add("ratings.score", 1).add("tags", 1).add("desc", 1);
//
//        DBCursor cursor = col.find(queryBuilder.get(), facetBuilder.get()).skip(page * pageSize)
//                .limit(pageSize).sort(BasicDBObjectBuilder.start("ratings.score", -1).get());
//
//        BasicDBList results = new BasicDBList();
//        while (cursor.hasNext()) {
//            DBObject loc = cursor.next();
//            results.add(loc);
//
//        }
//
//        return results;
//    }

//    public static List<JsonNode> getSuggestionsOld(POIType poiType, String word, int page, int pageSize) throws TravelPiException {
//        String colName;
//        switch (poiType) {
//            case VIEW_SPOT:
//                colName = "view_spot";
//                break;
//            case HOTEL:
//                colName = "hotel";
//                break;
//            case RESTAURANT:
//                colName = "restaurant";
//                break;
//            default:
//                return new ArrayList<>();
//        }
//
//        Pattern pattern = Pattern.compile("^" + word);
//        DBCollection colLoc;
//        try {
//            colLoc = Utils.getMongoClient().getDB("poi").getCollection(colName);
//        } catch (TravelPiException e) {
//            throw new TravelPiException(ErrorCode.DATABASE_ERROR, e.getMessage(), e);
//        }
//
//        DBObject qb = QueryBuilder.start("name").regex(pattern).get();
//        DBCursor cursor = colLoc.find(qb, BasicDBObjectBuilder.start("name", 1).add("geo", 1).get())
//                .sort(BasicDBObjectBuilder.start("ratings.score", -1).get()).limit(pageSize);
//        List<JsonNode> results = new ArrayList<>();
//        while (cursor.hasNext())
//            results.add(getPOIInfoJson(cursor.next(), 1));
//        return results;
//    }

//    /**
//     * 获得Json格式的POI信息。
//     *
//     * @param node
//     * @param level
//     * @return
//     */
//    public static ObjectNode getPOIInfoJson(DBObject node, int level) {
//        DBObject retVs = new BasicDBObject();
//
//        retVs.put("_id", node.get("_id").toString());
//        retVs.put("name", node.get("name").toString());
//        Object tmp;
//
//        tmp = node.get("geo");
//        if (tmp == null || !(tmp instanceof DBObject))
//            tmp = new BasicDBObject();
//        DBObject geoNode = (DBObject) tmp;
//        BasicDBObjectBuilder geoBuilder = BasicDBObjectBuilder.start();
//        geoBuilder.add("locId", geoNode.get("locId").toString()).add("locName", geoNode.get("locName"));
//        if (level >= 3) {
//            for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
//                tmp = geoNode.get(k);
//                if (tmp != null && (tmp instanceof Double))
//                    geoBuilder.add(k, tmp);
//                //edit by PC_Chen
////                else
////                    geoBuilder.add(k, "");
//            }
//        }
//        retVs.put("geo", geoBuilder.get());
//
//        if (level >= 2) {
//            BasicDBList retTagList = new BasicDBList();
//            tmp = node.get("tags");
//            if (tmp == null || !(tmp instanceof BasicDBList))
//                tmp = new BasicDBList();
//            for (Object tmp2 : (BasicDBList) tmp)
//                retTagList.add(tmp2.toString());
//            retVs.put("tags", retTagList);
//
//            BasicDBList retImageList = new BasicDBList();
//            tmp = node.get("imageList");
//            if (tmp == null || !(tmp instanceof BasicDBList))
//                tmp = new BasicDBList();
//            for (Object tmp2 : (BasicDBList) tmp)
//                retImageList.add(tmp2.toString());
//            retVs.put("imageList", retImageList);
//
//            if (level >= 3) {
//                tmp = node.get("price");
//                //edit by PC_Chen
////                retVs.put("cost", ((tmp == null || !(tmp instanceof Double)) ? "" : (double) tmp));
//                if (tmp != null || (tmp instanceof Double)) {
//                    retVs.put("cost", (Double) tmp);
//                }
//                tmp = node.get("priceDesc");
//                retVs.put("costDesc", (tmp == null ? "" : tmp.toString()));
//                //edit by PC_Chen
////                retVs.put("timeCost", "");
//            }
//        }
//
//        tmp = node.get("desc");
//        if (level == 2)
//            retVs.put("desc", (tmp == null ? "" : StringUtils.abbreviate(tmp.toString(), Constants.ABBREVIATE_LEN)));
//        else if (level >= 3)
//            retVs.put("desc", (tmp == null ? "" : tmp.toString()));
//
//        return (ObjectNode) Json.toJson(retVs);
//    }

    /**
     * 获得推荐的境外目的地
     */
    public static Map<String, List<Locality>> destRecommend() throws TravelPiException {
        List countryList = Utils.getMongoClient().getDB("geo").getCollection("Locality")
                .distinct(String.format("%s._id", Locality.fnCountry));

        Map<String, List<Locality>> results = new HashMap<>();
        for (Object obj : countryList) {
            Country country = GeoAPI.countryDetails((ObjectId) obj, Arrays.asList(Country.fnEnName, Country.fnZhName));
            if (country.getEnName().equals("China"))
                continue;

            List<Locality> l = new ArrayList<>();
            for (Iterator<Locality> itr = GeoAPI.searchLocalities("", false, country.id, 0, 10); itr.hasNext(); )
                l.add(itr.next());
            results.put(country.getZhName(), l);
        }
        return results;
    }

    /**
     * 获得POI信息。
     *
     * @param ids     POI的id。
     * @param poiType POI的类型。包括：view_spot: 景点；hotel: 酒店；restaurant: 餐厅。
     * @return POI详情。如果没有找到，返回null。
     */
    public static List<? extends AbstractPOI> getPOIInfoList(List<ObjectId> ids, String poiType, List<String> fieldList, int page, int pageSize) throws TravelPiException {
        Class<? extends AbstractPOI> poiClass;
        switch (poiType) {
            case "vs":
                poiClass = ViewSpot.class;
                break;
            case "hotel":
                poiClass = Hotel.class;
                break;
            case "shopping":
                poiClass = Shopping.class;
                break;
            case "restaurant":
                poiClass = Restaurant.class;
                break;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId tempId : ids) {
            criList.add(query.criteria("id").equal(tempId));
        }

        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    public static List<? extends AbstractPOI> getPOIInfoListByPOI(List<? extends AbstractPOI> pois, String poiType, List<String> fieldList, int page, int pageSize) throws TravelPiException {

        if (pois == null) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POIs.");
        }
        List<ObjectId> ids = new ArrayList<>();
        for (AbstractPOI temp : pois) {
            ids.add(temp.id);
        }
        return getPOIInfoList(ids, poiType, fieldList, page, pageSize);
    }

    /**
     * 获得景点周围的poi列表
     *
     * @param poiType
     * @param lat
     * @param lng
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static Iterator<? extends AbstractPOI> getPOINearBy(POIType poiType, Double lng, Double lat, int page, int pageSize) throws TravelPiException {
        Class<? extends AbstractPOI> poiClass;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        query = query.field(AbstractPOI.fnLocation).near(lng, lat, true);
        query.offset(page * pageSize).limit(pageSize);
        return query.iterator();
    }

    /**
     * 获取景点简介
     *
     * @param id
     * @param list
     * @return
     * @throws TravelPiException
     */
    public static ViewSpot getVsDetail(ObjectId id, List<String> list) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<ViewSpot> query = ds.createQuery(ViewSpot.class).field("_id").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 通过id获取景点简介和交通
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static ViewSpot getVsDetails(ObjectId id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<ViewSpot> query = ds.createQuery(ViewSpot.class).field("_id").equal(id);
        return query.get();
    }

    /**
     * 通过id返回游玩攻略
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static TravelGuide getTravelGuideApi(ObjectId id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<TravelGuide> query = ds.createQuery(TravelGuide.class).field("id").equal(id);
        return query.get();

    }


    /**
     * 获得地区的poi
     * 桃子旅行用,与旅行派的有区别
     *
     * @param poiType
     * @param locId
     * @param sortField
     * @param sort
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static List<? extends AbstractPOI> viewPoiList(POIType poiType, ObjectId locId, final SortField sortField,
                                                          Boolean sort, int page, int pageSize)
            throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Class<? extends AbstractPOI> poiClass = null;
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, "_id", "zhName", "enName", "rating", "images",
                "desc", "locList", "priceDesc", "address", "tags");
        switch (poiType) {
            case VIEW_SPOT:
                fieldList.add("timeCostDesc");
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                fieldList.add("telephone");
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Shopping.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        query = query.field("locList.id").equal(locId);
        query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        // 排序
        String stKey = null;
        switch (sortField) {
            case RATING:
                stKey = "rating";
                break;
        }
        query.order(String.format("%s%s", sort ? "" : "-", stKey));
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 根据关键词搜索POI
     * @param poiType
     * @param keyword
     * @param locId
     * @param prefix
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static List<? extends AbstractPOI> poiSearchForTaozi(POIType poiType, String keyword, ObjectId locId,
                                                                boolean prefix, int page, int pageSize)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Class<? extends AbstractPOI> poiClass = null;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Restaurant.class;
                break;
        }
        if (poiClass == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        if (keyword != null && !keyword.isEmpty()) {
            query.or(
                    query.criteria("zhName").equal(Pattern.compile(prefix ? "^" + keyword : keyword)),
                    query.criteria("enName").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE)),
                    query.criteria("alias").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE))
            );
        }
        if (locId != null)
            query.field("targets").equal(locId);
        query.order("-hotness");
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    public enum SortField {
        SCORE, PRICE, RATING
    }

    public enum POIType {
        VIEW_SPOT,
        HOTEL,
        RESTAURANT,
        SHOPPING,
        ENTERTAINMENT,
        DINNING
    }
}
