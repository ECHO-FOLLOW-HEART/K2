package models.guide;

import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.poi.Dinning;
import models.poi.Entertainment;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2014/11/4.
 */
public class Guide extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static final String FNSHOPPING="shoppings";
    @Transient
    public static final String FNDINNING="dinnings";
    public Integer userId;

    public String title;

    public List<ItinerItem> itinerary;

    public List<Shopping> shoppings;

    public List<Dinning> dinnings;
    @Override
    public JsonNode toJson() {
        return null;
    }
}
