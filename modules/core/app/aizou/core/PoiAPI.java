package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.AizouBaseEntity;
import database.MorphiaFactory;
import models.SolrServerFactory;
import models.geo.Country;
import models.geo.Locality;
import models.poi.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import utils.Constants;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * POI相关核心接口。
 *
 * @author Zephyre
 */
public class PoiAPI {

    public enum SortField {
        SCORE, PRICE, RATING, HOTNESS
    }

    public enum POIType {
        VIEW_SPOT,
        HOTEL,
        RESTAURANT,
        SHOPPING,
        ENTERTAINMENT,
        DINNING
    }

    public enum DestinationType {
        REMOTE_TRAFFIC,
        LOCAL_TRAFFIC,
        ACTIVITY,
        TIPS,
        GEOHISTORY,
        DINNING,
        SHOPPING,
        DESC,
        SPECIALS
    }

    /**
     * 获得POI联想列表。
     *
     * @param poiType
     * @param word
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> getSuggestions(POIType poiType, String word, int pageSize) throws AizouException {
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
                poiClass = Shopping.class;
                break;
        }
        if (poiClass == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Datastore ds = MorphiaFactory.datastore();
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        query.filter(AbstractPOI.FD_ALIAS, Pattern.compile("^" + word))
                .order(String.format("-%s, -%s", AbstractPOI.fnHotness, AbstractPOI.fnRating));
        return query.limit(pageSize).iterator();
    }

    public static java.util.Iterator<? extends AbstractPOI> poiSearch(POIType poiType,
                                                                      String keywords,
                                                                      ObjectId locId,
                                                                      Double lng,
                                                                      Double lat,
                                                                      Double maxDistance,
                                                                      String tag,
                                                                      int hotelType,
                                                                      final SortField sortField,
                                                                      boolean isSortAsc,
                                                                      int page, int pageSize)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        // POI类型
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
                poiClass = Shopping.class;
                break;
        }
        if (poiClass == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        // 排序
        String stKey = null;
        String defaultSorStr;
        if (sortField == null)
            defaultSorStr = String.format("%s,-%s, -%s", AbstractPOI.FD_RANK, AbstractPOI.fnHotness, AbstractPOI.fnRating);
        else {
            switch (sortField) {
                case PRICE:
                    stKey = "price";
                    break;
            }
            defaultSorStr = String.format("%s,-%s, -%s", (isSortAsc ? "" : "-") + stKey, AbstractPOI.fnHotness, AbstractPOI.fnRating);
        }

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        // 限定关键词
        if (keywords != null && !keywords.isEmpty())
            query = query.filter(AbstractPOI.FD_ZH_NAME, Pattern.compile(keywords));
        // 限定目的地
        if (locId != null)
            query.field("targets").equal(locId);
        //query.or(query.criteria("targets").equal(locId), query.criteria("addr.loc.id").equal(locId));

        // 限定周围距离
        if (lng != 0 && lat != 0)
            query.field(AbstractPOI.FD_LOCATION).near(lng, lat, maxDistance, true).field(AizouBaseEntity.FD_TAOZIENA).equal(true);

        // 限定标签
        if (tag != null && !tag.isEmpty())
            query = query.field("tags").equal(tag);

        // 限定酒店类型：空-类型不限 1-星级酒店 2-经济型酒店 3-青年旅社 4-民俗酒店
        if (hotelType != 0)
            query = query.field("type").equal(hotelType);

        String[] fieldList = {AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_EN_NAME, AbstractPOI.fnRating, AbstractPOI.detDesc, AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_TAGS, AbstractPOI.detContact, AbstractPOI.FD_PRICE, AbstractPOI.FD_ALIAS, AbstractPOI.FD_LOCALITY, AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RANK,AbstractPOI.FD_STYLE};
        query.retrievedFields(true, fieldList);
        if (lng == 0 && lat == 0)
            query.order(defaultSorStr);
        query.offset(page * pageSize).limit(pageSize);
        return query.iterator();
    }

    /**
     * 获得POI信息。
     *
     * @see PoiAPI#getPOIInfo(org.bson.types.ObjectId, PoiAPI.POIType, boolean)
     */
    public static AbstractPOI getPOIInfo(String poiId, POIType poiType, boolean showDetails) throws AizouException {
        return getPOIInfo(new ObjectId(poiId), poiType, showDetails);
    }

    /**
     * 获得POI信息。
     */
    public static <T extends AbstractPOI> T getPOIInfo(ObjectId poiId, Class<T> poiClass, Collection<String> fieldList)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<T> query = ds.createQuery(poiClass).field("_id").equal(poiId).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

        return query.get();
    }

    /**
     * 获得POI信息相关的推荐
     *
     * @param poiId
     * @return
     * @throws exception.AizouException
     */
    public static List<POIRmd> getPOIRmd(String poiId, int page, int pageSize) throws AizouException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.datastore();
        Query<POIRmd> query = ds.createQuery(POIRmd.class);
        query.field("poiId").equal(id).offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    /**
     * 获得POI信息相关的推荐条数
     *
     * @param poiId
     * @return
     * @throws exception.AizouException
     */
    public static long getPOIRmdCount(String poiId) throws AizouException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.datastore();
        Query<POIRmd> query = ds.createQuery(POIRmd.class);
        query.field("poiId").equal(id);
        return ds.getCount(query);
    }

    /**
     * 获得POI信息相关的评论
     *
     * @param poiId
     * @return
     * @throws exception.AizouException
     */
    public static List<Comment> getPOIComment(String poiId, int page, int pageSize) throws AizouException {

        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.datastore();
        Query<Comment> query = ds.createQuery(Comment.class);

        query.field(Comment.FD_ITEM_ID).equal(id).offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    /**
     * 获得POI信息相关的评论
     *
     * @param poiIds
     * @return
     * @throws exception.AizouException
     */
    public static List<Comment> getPOICommentByList(List<ObjectId> poiIds, int page, int pageSize) throws AizouException {
        if (poiIds == null || poiIds.isEmpty())
            return new ArrayList<>();
        Datastore ds = MorphiaFactory.datastore();
        Query<Comment> query = ds.createQuery(Comment.class);

        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId tempId : poiIds) {
            if (tempId != null)
                criList.add(query.criteria(Comment.FD_ITEM_ID).equal(tempId));
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
     * @throws exception.AizouException
     */
    public static long getPOICommentCount(String poiId) throws AizouException {
        ObjectId id = new ObjectId(poiId);
        Datastore ds = MorphiaFactory.datastore();
        Query<Comment> query = ds.createQuery(Comment.class);
        query.field(Comment.FD_ITEM_ID).equal(id);
        return ds.getCount(query);
    }

    /**
     * 获得POI信息。
     *
     * @param poiId       POI的id。
     * @param poiType     POI的类型。包括：view_spot: 景点；hotel: 酒店；restaurant: 餐厅。
     * @param showDetails 是否返回详情。
     * @return POI详情。如果没有找到，返回null。
     */
    public static AbstractPOI getPOIInfo(ObjectId poiId, POIType poiType, boolean showDetails) throws AizouException {
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
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.datastore();
        return ds.createQuery(poiClass).field("_id").equal(poiId).get();
    }

    /**
     * 获得POI详情（字段过滤）
     *
     * @param poiId
     * @param poiType
     * @param fields
     * @return
     * @throws exception.AizouException
     */
    public static AbstractPOI getPOIInfo(ObjectId poiId, POIType poiType, List<String> fields) throws AizouException {
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
            case ENTERTAINMENT:
                //TODO
                poiClass = Entertainment.class;
                break;
            default:
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.datastore();
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass).field("_id").equal(poiId);
        if (fields != null && !fields.isEmpty())
            query.retrievedFields(true, fields.toArray(new String[fields.size()]));
        return query.get();
    }

    /**
     * 发现POI。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> explore(POIType poiType, String locId,
                                                          int page, int pageSize) throws AizouException {
        try {
            return explore(poiType, new ObjectId(locId), false, page, pageSize);
        } catch (IllegalArgumentException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.",
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
                                                          boolean abroad, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();

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
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");

        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);
        if (locId != null) {
            if (poiType == POIType.HOTEL)
                query.field(AbstractPOI.detTargets).hasThisOne(locId);
            else
                query.or(query.criteria("targets").equal(locId), query.criteria("addr.loc.id").equal(locId));
        }

        return query.offset(page * pageSize).limit(pageSize).order(String.format("-%s", AbstractPOI.fnRating))
                .iterator();
    }

    public static LyMapping getTongChenPOI(ObjectId poiId) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<LyMapping> query = ds.createQuery(LyMapping.class);
        query.field("itemId").equal(poiId);
        return query.get();
    }

    /**
     * 获得POI信息。
     *
     * @param ids     POI的id。
     * @param poiType POI的类型。包括：view_spot: 景点；hotel: 酒店；restaurant: 餐厅。
     * @return POI详情。如果没有找到，返回null。
     */
    public static List<? extends AbstractPOI> getPOIInfoList(List<ObjectId> ids, String poiType, List<String> fieldList, int page, int pageSize) throws AizouException {
        if (ids.isEmpty() || ids == null)
            return new ArrayList<>();

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
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }

        Datastore ds = MorphiaFactory.datastore();
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

    /**
     * 获取景点简介
     *
     * @param id
     * @param list
     * @return
     * @throws exception.AizouException
     */
    public static ViewSpot getVsDetail(ObjectId id, List<String> list) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
     * @throws exception.AizouException
     */
    public static Locality getLocDetails(ObjectId id, List<String> list) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 获取特定字段的destination
     *
     * @param id
     * @return
     * @throws AizouException
     */
    public static Locality getLocalityByField(ObjectId id, List<String> fieldList) throws AizouException {

        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(id).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

        return query.get();
    }

    public static <T extends AbstractPOI> T getPOIByField(ObjectId id, List<String> fields, Class<T> poiClass)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<T> query = ds.createQuery(poiClass).field("_id").equal(id).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        if (fields != null && !fields.isEmpty())
            query.retrievedFields(true, fields.toArray(new String[fields.size()]));

        return query.get();

    }


    /**
     * 根据关键词搜索POI
     *
     * @param poiType
     * @param keyword
     * @param locId
     * @param prefix
     * @param page
     * @param pageSize
     * @return
     * @throws exception.AizouException
     */
    public static List<? extends AbstractPOI> poiSearchForTaozi(POIType poiType, String keyword, ObjectId locId,
                                                                boolean prefix, int page, int pageSize)
            throws AizouException, SolrServerException {
        Datastore ds = MorphiaFactory.datastore();
        Class<? extends AbstractPOI> poiClass = null;
        List<? extends AbstractPOI> poiList = null;

        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                poiList = poiSolrSearch(keyword, page, pageSize);
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
        }
        if (poiClass == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        Query<? extends AbstractPOI> query = ds.createQuery(poiClass);

        // TODO 暂时写成这样，等Solr的数据可以及时同步再改
        if (poiClass == ViewSpot.class && poiList != null) {
            List<ObjectId> poiIdList = new ArrayList<>();
            for (AbstractPOI aPoi : poiList) {
                poiIdList.add(aPoi.getId());
            }
            if (!poiIdList.isEmpty()) {
                query.field(AizouBaseEntity.FD_ID).in(poiIdList).order(String.format("-%s", AbstractPOI.fnHotness));
                if (keyword != null && !keyword.isEmpty()) {
                    keyword = keyword.toLowerCase();
                    query.field("alias").equal(Pattern.compile("^" + keyword));
                }
                if (locId != null)
                    query.field("targets").equal(locId);
                query.field(AizouBaseEntity.FD_TAOZIENA).equal(true);
                // 分页已在poiSolrSearch中完成
                query.offset(page * pageSize).limit(pageSize);
                return query.asList();
            } else
                return new ArrayList<>();
        }

        if (keyword != null && !keyword.isEmpty()) {
            keyword = keyword.toLowerCase();
            query.field("alias").equal(Pattern.compile("^" + keyword));
        }

        if (locId != null)
            query.field("targets").equal(locId);
        query.field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        query.order("-hotness");
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    public static List<? extends AbstractPOI> poiSolrSearch(String keyword, int page, int pageSize) throws SolrServerException {
        List<AbstractPOI> poiList = new ArrayList<>();

        SolrServer server = SolrServerFactory.getSolrInstance("viewspot");

        SolrQuery query = new SolrQuery();
        String queryString = String.format("alias:%s", keyword);
        query.setQuery(queryString);
        //query.setStart(page * pageSize).setRows(pageSize);
        //query.setSort(AbstractPOI.fnHotness, SolrQuery.ORDER.desc);
        //query.addFilterQuery("taoziEna:true");
        query.setRows(Constants.MAX_COUNT);
        query.setFields(AizouBaseEntity.FD_ID);
        SolrDocumentList vsDocs = server.query(query).getResults();

        //TODO 不查询数据库
        Object tmp;
        for (SolrDocument doc : vsDocs) {
            ViewSpot vs = new ViewSpot();
            //获取id
            vs.setId(new ObjectId(doc.get("id").toString()));
            //中文名
            tmp = doc.get("zhName");
            vs.zhName = (tmp == null ? null : (String) tmp);
            //英文名
            tmp = doc.get("enName");
            vs.enName = (tmp == null ? null : (String) tmp);
            //简介
            tmp = doc.get("desc");
            vs.desc = (tmp == null ? null : (String) tmp);
            //封面
            tmp = doc.get("images");
            vs.images = (tmp == null || ((List) tmp).isEmpty() ? null : (List) tmp);

            poiList.add(vs);
        }

        return poiList;
    }
}

