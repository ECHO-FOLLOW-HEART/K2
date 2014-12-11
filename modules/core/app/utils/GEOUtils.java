package utils;

import exception.AizouException;
import models.geo.Locality;
import org.bson.types.ObjectId;

import java.util.Hashtable;

/**
 * Created by topy on 2014/9/25.
 */
public class GEOUtils {

    private static GEOUtils ourInstance;

    //Key-目的地的LocId,Value-目的地附近省会
    private static Hashtable<ObjectId, Locality> dsMap = new Hashtable<>();

    public synchronized static GEOUtils getInstance() throws AizouException {
        if (ourInstance == null)
            ourInstance = new GEOUtils();

        return ourInstance;
    }

//    /**
//     * 找到距离最近的省会
//     *
//     * @param travelId
//     * @return
//     * @throws exception.TravelPiException
//     */
//    public synchronized Locality getNearCap(ObjectId travelId) throws TravelPiException {
//        if (null == travelId) {
//            return null;
//        }
//        if (dsMap.containsKey(travelId))
//            return dsMap.get(travelId);
//
//        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
//        //查询出路线的目的地
//        Query<Locality> queryLoc = ds.createQuery(Locality.class);
//        Locality travelLoc = queryLoc.field("_id").equal(travelId).get();
//
//        //如果本身就是省会，则最近的省会是北京
//        if (travelLoc.provCap) {
//            return ds.createQuery(Locality.class).field("_id").equal(new ObjectId("53aa9a6410114e3fd47833bd")).get();
//        }
//
//        //查询出所有省会
//        Query<Locality> query = ds.createQuery(Locality.class);
//        query.field("provCap").equal(Boolean.TRUE).field("level").equal(2);
//        Locality tempLocality = null;
//        Locality nearLocality = null;
//
//        int nearDistance = Constants.MAX_COUNT;
//        int tempDistance = 0;
//        for (Iterator<Locality> it = query.iterator(); it.hasNext(); ) {
//            tempLocality = it.next();
//            double[] travelLocCoords = travelLoc.location.getCoordinates();
//            double[] tempLocCoords = tempLocality.location.getCoordinates();
//            tempDistance = Utils.getDistatce(travelLocCoords[1], tempLocCoords[1], travelLocCoords[0], tempLocCoords[0]);
//            if (tempDistance < nearDistance) {
//                nearDistance = tempDistance;
//                nearLocality = tempLocality;
//            }
//        }
//        dsMap.put(travelId, nearLocality);
//        return nearLocality;
//    }
}
