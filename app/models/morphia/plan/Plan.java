package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
public class Plan extends TravelPiBaseItem {

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

    public List<Integer> travelMonth;

    public Integer totalCost;

    public Integer budget;

    /**
     * 注意事项
     */
    public String tips;

    @Embedded
    public CheckinRatings ratings;

    public List<PlanDayEntry> details;

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString());
        builder.add("target", target.toJson());
        for (String k : new String[]{"tags", "title", "days", "desc", "imageList"}) {
            Object val = null;
            try {
                val = Plan.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            builder.add(k, val != null ? val : "");
        }
        builder.add("ratings", ratings != null ? ratings.toJson() : "");
        builder.add("budget", 2000);

        if (showDetails) {
            List<JsonNode> detailsNodes = new ArrayList<>();
            if (details != null) {
                for (PlanDayEntry entry : details) {
                    detailsNodes.add(entry.toJson());
                }
            }
            builder.add("details", !detailsNodes.isEmpty() ? Json.toJson(detailsNodes) : new ArrayList<>());
        }

        return Json.toJson(builder.get());
    }
}
