package models.morphia.misc;

import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.Date;
import java.util.List;

/**
 * 天气
 *
 * @author Zephyre
 */
@Entity
public class Weather extends TravelPiBaseItem {
    /**
     * 当前天气
     */
    @Embedded
    public WeatherItem current;

    /**
     * 预报
     */
    public List<WeatherItem> forecast;

    /**
     * 更新时间
     */
    public Date updateDate;
}
