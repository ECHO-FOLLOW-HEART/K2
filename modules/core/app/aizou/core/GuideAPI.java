package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.MorphiaFactory;
import models.geo.Locality;
import models.guide.*;
import models.poi.AbstractPOI;
import models.poi.Restaurant;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import utils.Constants;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {

    /**
     * 根据ID取得攻略
     *
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideByDestination(List<ObjectId> ids, Integer userId) throws AizouException {
        Query<GuideTemplate> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(GuideTemplate.class);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId id : ids) {
            criList.add(query.criteria("locId").equal(id));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        Query<Locality> queryDes = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO)
                .createQuery(Locality.class);
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, "_id", "zhName", "enName");
        queryDes.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        List<CriteriaContainerImpl> criListDes = new ArrayList<>();
        for (ObjectId id : ids) {
            criListDes.add(queryDes.criteria("_id").equal(id));
        }
        queryDes.or(criListDes.toArray(new CriteriaContainerImpl[criListDes.size()]));

        List<Locality> destinations = queryDes.asList();
        List<GuideTemplate> guideTemplates = query.asList();

        Guide result = constituteUgcGuide(guideTemplates, destinations, userId);
        //创建时即保存
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE).save(result);
        return result;

    }

    /**
     * 合并模板攻略
     *
     * @param guideTemplates
     * @param destinations
     * @param userId
     * @return
     */
    private static Guide constituteUgcGuide(List<GuideTemplate> guideTemplates, List<Locality> destinations, Integer userId) {
        if (guideTemplates == null || guideTemplates.size() == 0)
            return new Guide();
        Guide ugcGuide = new Guide();
        ugcGuide.setId(new ObjectId());
        ugcGuide.userId = userId;
        int index = 0;
        List<ObjectId> locIds = new ArrayList<>();
        StringBuffer titlesBuffer = new StringBuffer();
        List<ItinerItem> itineraries = new ArrayList<>();
        List<Shopping> shoppingList = new ArrayList<>();
        List<Restaurant> restaurants = new ArrayList<>();
        Integer itineraryDaysCnt = 0;
        for (GuideTemplate temp : guideTemplates) {
            if (temp == null)
                continue;
            locIds.add(temp.getId());
            titlesBuffer.append(temp.title);
            if (temp.itinerary != null && temp.itinerary.size() > 0) {
                for (ItinerItem it : temp.itinerary) {
                    it.dayIndex = it.dayIndex + index;
                }
                itineraries.addAll(temp.itinerary);
                itineraryDaysCnt = itineraryDaysCnt + temp.itinerary.size();
            }
            if (temp.shopping != null && temp.shopping.size() > 0) {
                shoppingList.addAll(temp.shopping);
            }

            if (temp.restaurant != null && temp.restaurant.size() > 0) {
                restaurants.addAll(temp.restaurant);
            }
            index++;
        }
        ugcGuide.localities = destinations;
        ugcGuide.title = titlesBuffer.toString();
        ugcGuide.itinerary = itineraries;
        ugcGuide.shopping = shoppingList;
        ugcGuide.restaurant = restaurants;
        ugcGuide.itineraryDays = itineraryDaysCnt;
        ugcGuide.updateTime = System.currentTimeMillis();
        //取第一个目的地的图片
        ugcGuide.images = guideTemplates.get(0).images;
        return ugcGuide;

    }

    /**
     * 根据ID取得攻略
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideById(ObjectId id, List<String> fieldList) throws AizouException {
        Query<Guide> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(Guide.class);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.field("_id").equal(id);
        return query.get();
    }

    /**
     * 根据ID删除攻略
     *
     * @param id
     * @throws exception.AizouException
     */
    public static void deleteGuideById(ObjectId id) throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field("_id").equal(id);
        ds.delete(query);
    }

    /**
     * 根据用户ID取得攻略列表
     *
     * @param uid
     * @return
     * @throws exception.AizouException
     */
    public static List<Guide> getGuideByUser(Integer uid, List<String> fieldList, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field(Guide.fnUserId).equal(uid);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.offset(page * pageSize).limit(pageSize);
        query.order("-updateTime");
        return query.asList();
    }

    /**
     * 更新行程单
     *
     * @param guideId
     * @param guide
     * @throws exception.AizouException
     */
    public static void updateGuide(ObjectId guideId, Guide guide, Integer userId) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("id").equal(guideId).field("userId").equal(userId);
        if (query.iterator().hasNext()) {
            UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
            if (guide.itinerary != null) {
                update.set(AbstractGuide.fnItinerary, guide.itinerary);
                update.set(Guide.fnItineraryDays, guide.itinerary == null ? 0 : guide.itinerary.size());
            }
            if (guide.shopping != null)
                update.set(AbstractGuide.fnShopping, guide.shopping);
            if (guide.restaurant != null)
                update.set(AbstractGuide.fnRestaurant, guide.restaurant);
            update.set(Guide.fnUpdateTime, System.currentTimeMillis());
            ds.update(query, update);
        }
    }

    /**
     * 通过id返回Guide详情
     *
     * @param id
     * @param list
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideInfo(ObjectId id, List<String> list) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("_id").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 保存攻略标题
     *
     * @param id
     * @param title
     * @throws exception.AizouException
     */
    public static void saveGuideTitle(ObjectId id, String title) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnTitle, title);
        uo.set(Guide.fnUpdateTime, System.currentTimeMillis());
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 获得目的地的攻略信息
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static DestGuideInfo getDestinationGuideInfo(ObjectId id) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<DestGuideInfo> query = ds.createQuery(DestGuideInfo.class);
        query.field("locId").equal(id);
        return query.get();
    }

    /**
     * @return
     */
    public static Guide fillGuideInfo(Guide guide) throws AizouException {
        if (guide == null)
            return new Guide();
        AbstractPOI poi;
        String type;
        PoiAPI.POIType poiType;
        List<ItinerItem> itinerary = guide.itinerary;
        List<ItinerItem> newItinerary = new ArrayList<>();
        List<Shopping> shopping = guide.shopping;
        List<Restaurant> restaurant = guide.restaurant;
        if (itinerary != null && itinerary.size() > 0) {
            for (ItinerItem temp : itinerary) {
                type = temp.type;
                if(type == null)
                    continue;
                switch (type) {
                    case "vs":
                        poiType = PoiAPI.POIType.VIEW_SPOT;
                        break;
                    case "hotel":
                        poiType = PoiAPI.POIType.HOTEL;
                        break;
                    case "restaurant":
                        poiType = PoiAPI.POIType.RESTAURANT;
                        break;
                    case "shopping":
                        poiType = PoiAPI.POIType.SHOPPING;
                        break;
                    default:
                        throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", type));
                }
                poi = PoiAPI.getPOIInfo(temp.poi.getId(), poiType, true);
                if (poi == null) {
                    LogUtils.info(GuideAPI.class, String.format("POI is not exist.Id: %s, Type: %s.", temp.poi.getId().toString(), type));
                    continue;
                } else {
                    temp.poi = poi;
                    newItinerary.add(temp);
                }
            }
            guide.itinerary = newItinerary;
        }
        List<ObjectId> ids;
        if (shopping != null && shopping.size() > 0) {
            ids = new ArrayList();
            for (Shopping temp : shopping) {
                ids.add(temp.getId());
            }
            guide.shopping = (List<Shopping>) PoiAPI.getPOIInfoList(ids, "shopping",null,Constants.ZERO_COUNT , Constants.MAX_COUNT);
        }
        if (restaurant != null && restaurant.size() > 0) {
            ids = new ArrayList();
            for (Restaurant temp : restaurant) {
                ids.add(temp.getId());
            }
            guide.restaurant = (List<Restaurant>) PoiAPI.getPOIInfoList(ids, "restaurant",null,Constants.ZERO_COUNT , Constants.MAX_COUNT);
        }
        return guide;
    }

}
