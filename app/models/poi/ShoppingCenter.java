package models.poi;

import javax.persistence.Entity;
import java.sql.Time;

/**
 * 购物中心
 *
 * @author Zephyre
 */
@Entity
public class ShoppingCenter extends POI {
    public static Finder<Long, ShoppingCenter> finder = new Finder<>(Long.class, ShoppingCenter.class);

    /**
     * 购物中心评级。
     */
    public Float rating;

    /**
     * 营业时间（文字描述）。
     */
    public String openTime;

    /**
     * 开放时间。
     */
    public Time openTimeStart;

    /**
     * 关门时间。
     */
    public Time openTimeEnd;
}
