package models.morphia.poi;

import models.morphia.misc.CheckinRatings;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 酒店使用的评价数据。
 *
 * @author Zephyre
 */
@Embedded
public class HotelRatings extends CheckinRatings {
    public Integer starLevel;
}
