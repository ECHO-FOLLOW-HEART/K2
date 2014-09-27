package utils;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by topy on 2014/9/25.
 */
public class GEOUtils {

    private static GEOUtils ourInstance;

    //Key-目的地的LocId,Value-目的地附近省会
    private static Hashtable<ObjectId, Locality> dsMap = new Hashtable<>();

    /**
     * 找到距离最近的省会
     *
     * @param travelId
     * @return
     * @throws exception.TravelPiException
     */
    public synchronized Locality getNearCap(ObjectId travelId) throws TravelPiException {
        if (null == travelId) {
            return null;
        }
        if (dsMap.containsKey(travelId))
            return dsMap.get(travelId);

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        //查询出路线的目的地
        Query<Locality> queryLoc = ds.createQuery(Locality.class);
        Locality travelLoc = queryLoc.field("_id").equal(travelId).get();

        //如果本身就是省会，则最近的省会是北京
        if (travelLoc.provCap) {
            return ds.createQuery(Locality.class).field("_id").equal(new ObjectId("53aa9a6410114e3fd47833bd")).get();
        }

        //查询出所有省会
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("provCap").equal(Boolean.TRUE).field("level").equal(2);
        Locality tempLocality = null;
        Locality nearLocality = null;

        int nearDistance = Constants.MAX_COUNT;
        int tempDistance = 0;
        for (Iterator<Locality> it = query.iterator(); it.hasNext(); ) {
            tempLocality = (Locality) it.next();
            tempDistance = Utils.getDistatce(travelLoc.coords.lat, tempLocality.coords.lat, travelLoc.coords.lng, tempLocality.coords.lng);
            if (tempDistance < nearDistance) {
                nearDistance = tempDistance;
                nearLocality = tempLocality;
            }
        }
        dsMap.put(travelId, nearLocality);
        return nearLocality;
    }

    public synchronized static GEOUtils getInstance() throws TravelPiException {
        if (ourInstance == null)
            ourInstance = new GEOUtils();

        return ourInstance;
    }
}
