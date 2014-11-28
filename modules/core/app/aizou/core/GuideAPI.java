package aizou.core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Destination;
import models.guide.*;
import models.poi.Dinning;
import models.poi.Restaurant;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

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
     * @throws TravelPiException
     */
    public static Guide getGuideByDestination(List<ObjectId> ids, Integer userId) throws TravelPiException {
        Query<GuideTemplate> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(GuideTemplate.class);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId id : ids) {
            criList.add(query.criteria("locId").equal(id));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        Query<Destination> queryDes = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO)
                .createQuery(Destination.class);
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, "_id", "zhName", "enName");
        queryDes.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        List<CriteriaContainerImpl> criListDes = new ArrayList<>();
        for (ObjectId id : ids) {
            criListDes.add(queryDes.criteria("_id").equal(id));
        }
        queryDes.or(criListDes.toArray(new CriteriaContainerImpl[criListDes.size()]));

        List<Destination> destinations = queryDes.asList();
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
    private static Guide constituteUgcGuide(List<GuideTemplate> guideTemplates, List<Destination> destinations, Integer userId) {
        if (guideTemplates == null || guideTemplates.size() == 0)
            return new Guide();
        Guide ugcGuide = new Guide();
        ugcGuide.id = new ObjectId();
        ugcGuide.userId = userId;
        int index = 0;
        List<ObjectId> locIds = new ArrayList<>();
        StringBuffer titlesBuffer = new StringBuffer();
        List<ItinerItem> itineraries = new ArrayList<>();
        List<Shopping> shoppingList = new ArrayList<>();
        List<Restaurant> restaurants = new ArrayList<>();
        Integer itineraryDaysCnt = 0;
        for (GuideTemplate temp : guideTemplates) {
            locIds.add(temp.id);
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
        ugcGuide.destinations = destinations;
        ugcGuide.title = titlesBuffer.toString();
        ugcGuide.itinerary = itineraries;
        ugcGuide.shopping = shoppingList;
        ugcGuide.restaurant = restaurants;
        ugcGuide.itineraryDays = itineraryDaysCnt;
        ugcGuide.updateTime = System.currentTimeMillis();
        return ugcGuide;

    }

    /**
     * 根据ID取得攻略
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static Guide getGuideById(ObjectId id, List<String> fieldList) throws TravelPiException {
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
     * @throws TravelPiException
     */
    public static void deleteGuideById(ObjectId id) throws TravelPiException {

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
     * @throws TravelPiException
     */
    public static List<Guide> getGuideByUser(Integer uid, List<String> fieldList, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field(Guide.fnUserId).equal(uid);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 更新行程单
     *
     * @param guideId
     * @param guide
     * @throws TravelPiException
     */
    public static void updateGuide(ObjectId guideId, Guide guide, Integer userId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("id").equal(guideId).field("userId").equal(userId);
        if (query.iterator().hasNext()) {
            UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
            if (guide.itinerary != null)
                update.set(AbstractGuide.fnItinerary, guide.itinerary);
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
     * @throws TravelPiException
     */
    public static Guide getGuideInfo(ObjectId id, List<String> list) throws TravelPiException {
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
     * @throws TravelPiException
     */
    public static void saveGuideTitle(ObjectId id, String title) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnTitle, title);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 获得目的地的攻略信息
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static DestGuideInfo getDestinationGuideInfo(ObjectId id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<DestGuideInfo> query = ds.createQuery(DestGuideInfo.class);
        query.field("locId").equal(id);
        return query.get();
    }
}
