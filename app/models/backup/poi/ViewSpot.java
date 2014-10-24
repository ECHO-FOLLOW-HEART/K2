package models.backup.poi;

import javax.persistence.Entity;

/**
 * 景点。
 *
 * @author Zephyre
 */
@Entity
public class ViewSpot extends POI {
    public static Finder<Long, ViewSpot> finder = new Finder<>(Long.class, ViewSpot.class);

    /**
     * 景点评级。
     */
    public Float rating;
}
