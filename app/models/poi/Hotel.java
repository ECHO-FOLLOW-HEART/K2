package models.poi;

import models.misc.HotelImage;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * 酒店
 *
 * @author Zephyre
 */
@Entity
public class Hotel extends POI {
    public static Finder<Long, Hotel> finder = new Finder<>(Long.class, Hotel.class);

    /**
     * 酒店评级。
     */
    public Float rating;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    public List<HotelImage> imgList;

}
