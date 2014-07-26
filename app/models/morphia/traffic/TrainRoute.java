package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
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
    public Map<String, Double> price;

    /**
     * 列车车次的类型。包括但不限于：
     * T：特快
     * D：动车
     * G：高铁
     */
    public String type;

    public List<TrainEntry> details;

    @Override
    public JsonNode toJson() {
        ObjectNode ret = (ObjectNode) super.toJson();


        return ret;
    }
}
