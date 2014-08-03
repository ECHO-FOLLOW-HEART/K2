package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
public class Plan extends TravelPiBaseItem implements ITravelPiFormatter {
    /**
     * 表明该UGC路线是基于哪一条模板。
     */
    public ObjectId templateId;

    @Embedded
    public List<SimpleRef> targets;

    public List<String> tags;

    public String title;

    public Integer planId;

    public Integer days;

    public String desc;

    public List<String> imageList;

    public List<Integer> travelMonth;

    public Integer totalCost;

    public List<Integer> budget;

    /**
     * 人工标记的路线优先级
     */
    public Integer manualPriority;

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
        builder.add("templateId", (templateId != null ? templateId.toString() : ""));

        List<JsonNode> targetList = new ArrayList<>();
        for (SimpleRef t : targets)
            targetList.add(t.toJson());

        if (targetList.size() > 0) {
            builder.add("target", targetList.get(0));
            builder.add("targets", Json.toJson(targetList));
        }
        builder.add("tags", (tags != null && !tags.isEmpty()) ? Json.toJson(tags) : new ArrayList<>());
        builder.add("title", (title != null && !title.isEmpty()) ? title : "");
        if (days != null)
            builder.add("days", days);
        builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");
        builder.add("ratings", ratings != null ? ratings.toJson() : Json.newObject());
        builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? Json.toJson(imageList) : new ArrayList<>());
        if (budget != null && !budget.isEmpty())
            builder.add("budget", budget);
        else
            builder.add("budget", new ArrayList<>());
//        builder.add("budget", Arrays.asList(2000, 3000));

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
