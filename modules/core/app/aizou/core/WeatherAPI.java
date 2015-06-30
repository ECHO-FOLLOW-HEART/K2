package aizou.core;

import exception.AizouException;
import database.MorphiaFactory;
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
    public static YahooWeather weatherDetails(ObjectId id) throws AizouException {
        return MorphiaFactory.datastore().createQuery(YahooWeather.class).field("loc.id").equal(id).get();
    }

}
