package models.poi;

import javax.persistence.Entity;

/**
 * 餐厅。
 *
 * @author Haizi
 */
@Entity
public class Restaurant extends POI {
    public static Finder<Long, Restaurant> finder = new Finder<>(Long.class, Restaurant.class);

    /**
     * 餐厅评级。
     */
    public Float rating;
}
