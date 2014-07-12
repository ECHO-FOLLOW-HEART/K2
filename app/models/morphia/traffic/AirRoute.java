package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import play.data.validation.Constraints;

/**
 * 航线。
 *
 * @author Zephyre
 */
@Entity
public class AirRoute extends TravelPiBaseItem{

    @Id
    public ObjectId id;

    @Constraints.Required
    @Reference
    public Airport depAirport;

    @Constraints.Required
    @Reference
    public Airport arrAirport;

    @Constraints.Required
    @Reference
    public Locality depLoc;

    @Constraints.Required
    @Reference
    public Locality arrLoc;

    public int distance;

    public String flightCode;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
