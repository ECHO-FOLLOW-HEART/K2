package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;
import models.morphia.misc.Ratings;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 酒店。
 *
 * @author Zephyre
 */
@Entity
public class Hotel extends AbstractPOI {
    @Override
    public JsonNode toJson() {
        return null;
    }
}
