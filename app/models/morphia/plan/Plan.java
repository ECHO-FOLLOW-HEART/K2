package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.misc.SimpleRef;
import models.morphia.poi.Ratings;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
public class Plan extends TravelPiBaseItem{

    @Id
    public ObjectId id;

    @Embedded
    public SimpleRef target;

    public List<String> tags;

    public String title;

    public Integer planId;

    public Integer days;

    public String desc;

    public List<String> imageList;

    @Embedded
    public Ratings ratings;

    public List<PlanDayEntry> details;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
