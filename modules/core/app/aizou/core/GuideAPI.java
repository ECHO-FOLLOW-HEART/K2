package aizou.core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.guide.Guide;
import models.guide.ItinerItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

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
     * 保存行程单
     *
     * @param guide
     * @param itemBeanList
     * @throws TravelPiException
     */
    public static void saveItinerary(Guide guide, List<ItinerItem> itemBeanList) throws TravelPiException {
        if (guide == null || itemBeanList == null || itemBeanList.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Null guide cannot save.");
        guide.itinerary = itemBeanList;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        ds.save(guide);
    }


}
