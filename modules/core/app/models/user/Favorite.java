package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.geo.Locality;
import models.misc.TravelNote;
import models.poi.Hotel;
import models.poi.Restaurant;
import models.poi.ViewSpot;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户的收藏夹
 * <p/>
 * Created by topy on 2014/10/27.
 */
@Entity
@JsonFilter("favoriteFilter")
public class Favorite extends TravelPiBaseItem {

    @Transient
    public static String fnViewSpot = "vs";

    @Transient
    public static String fnHotel = "hotel";

    @Transient
    public static String fnRestaurant = "restaurant";

    @Transient
    public static String fnTravelNote = "travelNote";

    @Transient
    public static String fnLocality = "locality";

    @Transient
    public static String fnUserId = "userId";
    /**
     * 收藏游记
     */
    @Embedded
    public List<TravelNote> travelNote;
    /**
     * 收藏城市
     */
    @Embedded
    public List<Locality> locality;
    /**
     * 用户ID
     */
    @Constraints.Required
    private Integer userId;
    /**
     * 收藏景点
     */
    @Embedded
    private List<ViewSpot> vs;
    /**
     * 收藏酒店
     */
    @Embedded
    private List<Hotel> hotel;
    /**
     * 收藏餐厅
     */
    @Embedded
    private List<Restaurant> restaurant;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer val) {
        userId = val;
    }

    public List<ViewSpot> getVs() {
        return (vs != null ? vs : new ArrayList<ViewSpot>());
    }

    public void setVs(List<ViewSpot> val) {
        vs = val;
    }

    public List<Hotel> getHotel() {
        return (hotel != null ? hotel : new ArrayList<Hotel>());
    }

    public void setHotel(List<Hotel> val) {
        hotel = val;
    }

    public List<Restaurant> getRestaurant() {
        return (restaurant != null ? restaurant : new ArrayList<Restaurant>());
    }

    public void setRestaurant(List<Restaurant> val) {
        restaurant = val;
    }

    public List<TravelNote> getTravelNote() {
        return (travelNote != null ? travelNote : new ArrayList<TravelNote>());
    }

    public void setTravelNote(List<TravelNote> val) {
        travelNote = val;
    }

    public List<Locality> getLocality() {
        return (locality != null ? locality : new ArrayList<Locality>());
    }

    public void setLocality(List<Locality> val) {
        locality = val;
    }
}
