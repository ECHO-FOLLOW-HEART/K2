package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 列车路线。
 *
 * @author Zephyre
 */
@Entity
public class TrainRoute extends AbstractRoute {
    @Id
    public ObjectId id;

    @Constraints.Required
    @Embedded
    public SimpleRef depStop;

    @Constraints.Required
    @Embedded
    public SimpleRef arrStop;

    @Constraints.Required
    @Embedded
    public SimpleRef depLoc;

    @Constraints.Required
    @Embedded
    public SimpleRef arrLoc;

    public int distance;

    @Constraints.Required
    public String code;

    public Map<String, Double> price;

    @Constraints.Required
    public int timeCost;

    @Constraints.Required
    public Date depTime;

    @Constraints.Required
    public Date arrTime;

    public String type;

    public List<TrainEntry> details;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
