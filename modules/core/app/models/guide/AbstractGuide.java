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
    public static final String fnLocalityItems = "localityItems";
    @Transient
    public static final String fnDemoItems = "demoItems";
    @Transient
    public static final String fnTrafficItems = "trafficItems";

    @Transient
    public static final String fnItinerary = "itinerary";

    @Transient
    public static final String fnShopping = "shopping";

    @Transient
    public static final String fnRestaurant = "restaurant";

    @Transient
    public static final String fnHotel = "hotel";

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

    public List<LocalityItem> localityItems;

    public List<DemoItem> demoItems;

    public List<TrafficItem> trafficItems;

    public List<Shopping> shopping;

    public List<Restaurant> restaurant;

    public List<ImageItem> images;

    public List<ImageItem> getImages() {
        if (images == null)
            return new ArrayList<>();
        return images;
    }

    public List<DemoItem> getDemoItems() {
        return demoItems;
    }

    public void setDemoItems(List<DemoItem> demoItems) {
        this.demoItems = demoItems;
    }

    public List<TrafficItem> getTrafficItems() {
        return trafficItems;
    }

    public void setTrafficItems(List<TrafficItem> trafficItems) {
        this.trafficItems = trafficItems;
    }

    public List<LocalityItem> getLocalityItems() {
        return localityItems;
    }

    public void setLocalityItems(List<LocalityItem> localityItems) {
        this.localityItems = localityItems;
    }

    public List<ItinerItem> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItinerItem> itinerary) {
        this.itinerary = itinerary;
    }
}
