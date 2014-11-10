package aizou.core;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.YahooWeather;
import org.bson.types.ObjectId;

/**
 * @author lxf
 * Weather相关的api
 */
public class WeatherAPI {

    /**
     * 获得城市天气的详情
     *
     * @param id
     */
    public static YahooWeather weatherDetails(ObjectId id) throws TravelPiException {
        return MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC).
                createQuery(YahooWeather.class).field("loc.id").equal(id).get();
    }

}
