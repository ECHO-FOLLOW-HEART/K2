package aizou.core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.poi.Dinning;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {

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
        query.field("_id").equal(id);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

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
     * @param itemBeanList
     * @throws TravelPiException
     */
    public static void updateItinerary(ObjectId guideId, List<ItinerItem> itemBeanList) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("id").equal(guideId);
        UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
        update.set(Guide.fnItinerary, itemBeanList);
        ds.update(query, update);
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
        try {
            title = title.trim();
            if (title.isEmpty())
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID TITLE");
        } catch (NullPointerException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID TITLE");
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnTitle, title);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 保存购物信息
     *
     * @param id
     * @param shoppingList
     * @throws TravelPiException
     */
    public static void savaGuideShopping(ObjectId id, List<Shopping> shoppingList) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnShopping, shoppingList);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 保存美食信息
     *
     * @param id
     * @param dinningList
     * @throws TravelPiException
     */
    public static void savaGuideDinning(ObjectId id, List<Dinning> dinningList) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnDinning, dinningList);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }
}
