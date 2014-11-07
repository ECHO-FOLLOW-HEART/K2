package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.poi.Dinning;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 攻略
 * Created by topy on 2014/11/4.
 */
@Entity
@JsonFilter("guideFilter")
public class Guide extends TravelPiBaseItem {

    @Transient
    public static final String fnUserId = "userId";

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

    public Integer userId;

    public String title;

    public ObjectId locId;

    public List<ItinerItem> itinerary;

    public List<Shopping> shopping;

    public List<Dinning> dinning;
}
