package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
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
abstract public class AbstractPlan extends AizouBaseEntity implements ITravelPiFormatter {
    @Transient
    public static final String FD_TARGETS = "targets";

    @Transient
    public static final String FD_TAGS = "tags";

    @Transient
    public static final String FD_COMPANY = "company";

    @Transient
    public static final String FD_DETAILS = "details";

    @Transient
    public static final String FD_DAYS = "days";

    @Transient
    public static final String FD_MANUAL_PRIORITY = "manualPriority";

    @Transient
    public static final String FD_TITLE = "title";

    @Transient
    public static final String FD_IMAGES = "images";

    /**
     * 目的地
     */
    @Embedded
    private List<SimpleRef> targets;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 路线标题
     */
    private String title;

    /**
     * 对应的原始id
     */
    private Integer planId;

    /**
     * 行程天数
     */
    private Integer days;

    /**
     * 景点个数
     */
    private Integer vsCnt;

    /**
     * 路线简介
     */
    private String desc;

    @Embedded
    private Description description;

    private List<String> imageList;

    private List<ImageItem> images;

    private List<Integer> travelMonth;

    private Integer totalCost;

    private List<Integer> budget;

    private Integer stayBudget;

    private Integer trafficBudget;

    private Integer viewBudget;

    /**
     * 人工标记的路线优先级
     */
    private Integer manualPriority;

    /**
     * 注意事项
     */
    private String tips;

    /**
     * 路线速览
     */
    private List<String> summary;

    /**
     * 人工编辑的路线标签：最省钱……
     */
    private List<String> lxpTag;

    @Embedded
    private CheckinRatings ratings;

    private List<PlanDayEntry> details;

    /**
     * 更多的描述
     */
    private String moreDesc;

    /**
     * 标签
     */
    private List<String> company;

    public List<SimpleRef> getTargets() {
        return targets;
    }

    public void setTargets(List<SimpleRef> targets) {
        this.targets = targets;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getVsCnt() {
        return vsCnt;
    }

    public void setVsCnt(Integer vsCnt) {
        this.vsCnt = vsCnt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public List<Integer> getTravelMonth() {
        return travelMonth;
    }

    public void setTravelMonth(List<Integer> travelMonth) {
        this.travelMonth = travelMonth;
    }

    public Integer getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Integer totalCost) {
        this.totalCost = totalCost;
    }

    public List<Integer> getBudget() {
        return budget;
    }

    public void setBudget(List<Integer> budget) {
        this.budget = budget;
    }

    public Integer getStayBudget() {
        return stayBudget;
    }

    public void setStayBudget(Integer stayBudget) {
        this.stayBudget = stayBudget;
    }

    public Integer getTrafficBudget() {
        return trafficBudget;
    }

    public void setTrafficBudget(Integer trafficBudget) {
        this.trafficBudget = trafficBudget;
    }

    public Integer getViewBudget() {
        return viewBudget;
    }

    public void setViewBudget(Integer viewBudget) {
        this.viewBudget = viewBudget;
    }

    public Integer getManualPriority() {
        return manualPriority;
    }

    public void setManualPriority(Integer manualPriority) {
        this.manualPriority = manualPriority;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public List<String> getSummary() {
        return summary;
    }

    public void setSummary(List<String> summary) {
        this.summary = summary;
    }

    public List<String> getLxpTag() {
        return lxpTag;
    }

    public void setLxpTag(List<String> lxpTag) {
        this.lxpTag = lxpTag;
    }

    public CheckinRatings getRatings() {
        return ratings;
    }

    public void setRatings(CheckinRatings ratings) {
        this.ratings = ratings;
    }

    public List<PlanDayEntry> getDetails() {
        return details;
    }

    public void setDetails(List<PlanDayEntry> details) {
        this.details = details;
    }

    public String getMoreDesc() {
        return moreDesc;
    }

    public void setMoreDesc(String moreDesc) {
        this.moreDesc = moreDesc;
    }

    public List<String> getCompany() {
        return company;
    }

    public void setCompany(List<String> company) {
        this.company = company;
    }

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", getId().toString());


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

        builder.add("desc", desc == null ? "" : desc);

        builder.add("ratings", ratings != null ? ratings.toJson() : Json.newObject());


        if (images == null) {
            builder.add("imageList", new ArrayList<>());
        } else {
            //ImageItem theImg = images.get(new Random().nextInt(images.size()));
            ImageItem theImg = images.get(0);
            String imgUrl = theImg.getFullUrl();
            if (imgUrl == null)
                imgUrl = theImg.getUrl();
            builder.add("imageList", Arrays.asList(imgUrl));
        }


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
            if (planDayEntry.actv == null)
                continue;
            List<String> components = new ArrayList<>();

            for (PlanItem planItem : planDayEntry.actv) {
                if (planItem.type == null || planItem.type.equals("traffic"))
                    continue;
                if (planItem.type.equals("vs"))
                    tempVsCount++;
                components.add(planItem.item.zhName);
            }
            if (!components.isEmpty())
                summaryList.add(StringUtils.join(components, "-"));
        }
        summary = summaryList;
        vsCnt = tempVsCount;
    }
}