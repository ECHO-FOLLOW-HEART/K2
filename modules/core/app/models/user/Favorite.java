package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.misc.TravelNote;
import models.poi.Hotel;
import models.poi.Restaurant;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topy on 2014/10/27.
 */
@Entity
@JsonFilter("favoriteFilter")
public class Favorite extends TravelPiBaseItem implements ITravelPiFormatter {

    @Transient
    public static String TYPE_VS = "vs";

    @Transient
    public static String TYPE_HOTEL = "hotel";

    @Transient
    public static String TYPE_RESTAURANT = "restaurant";

    @Transient
    public static String TYPE_TRAVELNOTE = "travelNote";

    @Id
    public ObjectId id;

    /**
     * 用户ID
     */
    @Constraints.Required
    public Integer userId;

    /**
     * 收藏景点
     */
    @Embedded
    public List<ViewSpot> vs;

    /**
     * 收藏酒店
     */
    @Embedded
    public List<Hotel> hotel;

    /**
     * 收藏餐厅
     */
    @Embedded
    public List<Restaurant> restaurant;

    /**
     * 收藏游记
     */
    @Embedded
    public List<TravelNote> travelNote;

    public List<ViewSpot> getVs() {
        if (vs == null)
            return new ArrayList<>();
        else
            return vs;
    }

    public List<Hotel> getHotel() {
        if (hotel == null)
            return new ArrayList<>();
        else
            return hotel;
    }

    public List<Restaurant> getRestaurant() {
        if (restaurant == null)
            return new ArrayList<>();
        else
            return restaurant;
    }

    public List<TravelNote> getTravelNote() {
        if (travelNote == null)
            return new ArrayList<>();
        else
            return travelNote;
    }

    @Override
    public JsonNode toJson() {
        return null;
    }
}
