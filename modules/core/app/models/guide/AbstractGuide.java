package models.guide;

import models.TravelPiBaseItem;
import models.poi.Dinning;
import models.poi.Restaurant;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2014/11/8.
 */
public abstract class AbstractGuide extends TravelPiBaseItem {

    @Transient
    public static final String fnTitle = "title";

    @Transient
    public static final String fnItinerary = "itinerary";

    @Transient
    public static final String fnShopping = "shopping";

    @Transient
    public static final String fnRestaurant = "restaurant";

    @Transient
    public static final String fdId = "id";

    @Transient
    public static final String fnLocId = "locId";

    public String title;

    public ObjectId locId;

    public List<ItinerItem> itinerary;

    public List<Shopping> shopping;

    public List<Restaurant> restaurant;

    public String getLocId() {
        return locId.toString();
    }
}
