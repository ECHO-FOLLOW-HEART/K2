package models.guide;

import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.Restaurant;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topy on 2014/11/8.
 */
public abstract class AbstractGuide extends AizouBaseEntity {

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
    public static final String fdLocId = "locId";

    @Transient
    public static final String fnLocalities = "localities";

    @Transient
    public static final String fnImages = "images";

    public String title;

    public ObjectId locId;

    public List<Locality> localities;

    public List<ItinerItem> itinerary;

    public List<Shopping> shopping;

    public List<Restaurant> restaurant;

    public List<ImageItem> images;

    public List<ImageItem> getImages() {
        if (images == null)
            return new ArrayList<>();
        return images;
    }

}
