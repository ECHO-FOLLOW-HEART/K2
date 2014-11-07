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
    public static String fdUserId = "userId";

    @Transient
    public static String fdTitle = "title";

    @Transient
    public static String fdItinerary = "itinerary";

    @Transient
    public static String fdShopping = "shopping";
    @Transient
    public static String fdDinning = "dinning";
    @Transient
    public static String fdId = "id";

    public Integer userId;

    public String title;

    public ObjectId locId;

    public List<ItinerItem> itinerary;

    public List<Shopping> shoppings;

    public List<Dinning> dinnings;

}
