package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

/**
 * 航空公司。
 *
 * @author Zephyre
 */
@Entity
public class Airline extends TravelPiBaseItem{

    @Id
    public ObjectId id;

    @Constraints.Required
    public String code;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String fullName;

    public String shortName;

    @Embedded
    public Contact contact;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
