package models.guide;

import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;
import java.util.List;

/**
 * Created by topy on 2014/11/4.
 */
@Embedded
public class ItinerItem extends TravelPiBaseItem implements ITravelPiFormatter {

    public Integer dayIndex;

    public String type;

    @Embedded
    public AbstractPOI poi;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
