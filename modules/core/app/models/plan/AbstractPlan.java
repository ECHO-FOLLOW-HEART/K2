package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.misc.CheckinRatings;
import models.misc.Description;
import models.misc.ImageItem;
import models.misc.SimpleRef;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
abstract public class AbstractPlan extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static final String FD_TARGETS = "targets";

    @Transient
    public static final String FD_TAGS = "tags";

    @Transient
    public static final String FD_DETAILS = "details";

    @Transient
    public static final String FD_DAYS = "days";

    @Transient
    public static final String FD_MANUAL_PRIORITY = "manualPriority";

    @Embedded
    public List<SimpleRef> targets;

    public List<String> tags;

    public String title;

    public Integer planId;


    /**
     * 行程天数
     */
    public Integer days;

    /**
     * 景点个数
     */
    public Integer vsCnt;

    public String desc;

    @Embedded
    public Description description;

    public List<String> imageList;

    public List<ImageItem> images;

    public List<Integer> travelMonth;

    public Integer totalCost;

    public List<Integer> budget;

    public Integer stayBudget;

    public Integer trafficBudget;

    public Integer viewBudget;

    /**
     * 人工标记的路线优先级
     */
    public Integer manualPriority;

    /**
     * 注意事项
     */
    public String tips;

    /**
     * 路线速览
     */
    public List<String> summary;

    /**
     * 人工编辑的路线标签：最省钱……
     */
    public List<String> lxpTag;

    @Embedded
    public CheckinRatings ratings;

    public List<PlanDayEntry> details;

    public String moreDesc;


    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString());


        List<JsonNode> targetList = new ArrayList<>();
        if (null != targets) {
            for (SimpleRef t : targets)
                targetList.add(t.toJson());

            if (targetList.size() > 0) {
                builder.add("target", targetList.get(0));
                builder.add("targets", Json.toJson(targetList));
            }
        }
        builder.add("tags", (tags != null && !tags.isEmpty()) ? Json.toJson(tags) : new ArrayList<>());
        builder.add("title", (title != null && !title.isEmpty()) ? title : "");
        if (days != null)
            builder.add("days", days);
        if (null != description && null != description.desc) {
            builder.add("desc", description.desc);
        }
        builder.add("ratings", ratings != null ? ratings.toJson() : Json.newObject());


        builder.add("imageList", images != null ? Arrays.asList(images.get(new Random().nextInt(images.size())).url) :
                new ArrayList<>());

        Integer tempStayBudget = stayBudget == null ? 0 : stayBudget;
        Integer tempTrafficBudget = trafficBudget == null ? 0 : trafficBudget;
        Integer tempViewBudget = viewBudget == null ? 0 : viewBudget;
        builder.add("stayBudget", tempStayBudget);
        builder.add("trafficBudget", tempTrafficBudget);
        builder.add("viewBudget", tempViewBudget);
        Integer total = tempStayBudget + tempTrafficBudget + tempViewBudget;
        Integer addTotal = total * 2;
        total = (int) (Math.round(total / 100.0) * 100);
        addTotal = (int) (Math.round(addTotal / 80.0) * 100);
        builder.add("budget", Arrays.asList(total, addTotal));

        List<String> ret = new ArrayList<>();
        if (null != lxpTag)
            for (String tag : lxpTag) {
                ret.add(tag);
            }
        builder.add("lxpTag", ret);

        if (showDetails) {
            List<JsonNode> detailsNodes = new ArrayList<>();
            if (details != null) {
                for (PlanDayEntry entry : details) {
                    detailsNodes.add(entry.toJson());
                }
            }
            builder.add("moreDesc", moreDesc == null ? "" : moreDesc);
            builder.add("details", !detailsNodes.isEmpty() ? Json.toJson(detailsNodes) : new ArrayList<>());
        }


        this.buildSummary(details);
        builder.add("summary", summary);
        builder.add("vsCnt", vsCnt);


        return Json.toJson(builder.get());
    }

    /**
     * 生成路线摘要。
     *
     * @param details
     */
    private void buildSummary(List<PlanDayEntry> details) {
        List<String> summaryList = new ArrayList<>();
        int tempVsCount = 0;
        for (PlanDayEntry planDayEntry : details) {
            List<String> components = new ArrayList<>();

            for (PlanItem planItem : planDayEntry.actv) {
                if (planItem.type == null || planItem.type.equals("traffic"))
                    continue;
                if (planItem.type.equals("vs"))
                    tempVsCount++;
                components.add(planItem.item.zhName);
            }
            summaryList.add(StringUtils.join(components, "-"));
        }
        summary = summaryList;
        vsCnt = tempVsCount;
    }
}