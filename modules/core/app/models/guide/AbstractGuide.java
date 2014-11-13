package models.guide;

import models.TravelPiBaseItem;
import models.poi.Dinning;
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
    public static final String fnDinning = "dinning";

    @Transient
    public static final String fdId = "id";

    public String title;

    public ObjectId locId;

    public List<ItinerItem> itinerary;

    public List<Shopping> shopping;

    public List<Dinning> dinning;
}
