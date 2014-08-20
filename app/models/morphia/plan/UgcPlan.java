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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
public class UgcPlan extends Plan implements ITravelPiFormatter {

    /**
     * 用户ID
     */
    public ObjectId uid;
    /**
     * 表明该UGC路线是基于哪一条模板。
     */
    public ObjectId templateId;
    /**
     * 出发时间。
     */
    public Date startDate;
    /**
     * 返程时间。
     */
    public Date endDate;

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        // TODO 调用父类的方法，不要重写一遍：  ObjectNode node = (ObjectNode) super.toJson();
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString());
        builder.add("uid", uid.toString());
        builder.add("templateId", (templateId != null ? templateId.toString() : ""));
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        builder.add("title", title.toString());
        builder.add("startDate", fmt.format(startDate));
        builder.add("endDate", fmt.format(endDate));

//        List<JsonNode> targetList = new ArrayList<>();
//        for (SimpleRef t : targets)
//            targetList.add(t.toJson());
//
//        if (targetList.size() > 0) {
//            builder.add("target", targetList.get(0));
//            builder.add("targets", Json.toJson(targetList));
//        }
//        builder.add("tags", (tags != null && !tags.isEmpty()) ? Json.toJson(tags) : new ArrayList<>());
//        builder.add("title", (title != null && !title.isEmpty()) ? title : "");
//        if (days != null)
//            builder.add("days", days);
//        builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");
//        builder.add("ratings", ratings != null ? ratings.toJson() : Json.newObject());
//        builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? Json.toJson(imageList) : new ArrayList<>());
//
//
//        Integer tempStayBudget = stayBudget == null ? 0 : stayBudget;
//        Integer tempTrafficBudget = trafficBudget == null ? 0 : trafficBudget;
//        Integer tempViewBudget = viewBudget == null ? 0 : viewBudget;
//        builder.add("stayBudget", tempStayBudget);
//        builder.add("trafficBudget", tempTrafficBudget);
//        builder.add("viewBudget", tempViewBudget);
//        Integer total = tempStayBudget + tempTrafficBudget + tempViewBudget;
//        Integer addTotal = total * 2;
//        total = (int) (Math.round(total / 100.0) * 100);
//        addTotal = (int) (Math.round(addTotal / 80.0) * 100);
//        builder.add("budget", Arrays.asList(total, addTotal));

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
