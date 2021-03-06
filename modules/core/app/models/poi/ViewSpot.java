package models.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import models.misc.Description;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;
import utils.DataFilter;

import java.util.*;


/**
 * 景点信息。
 *
 * @author Zephyre
 */
public class ViewSpot extends AbstractPOI {

    @Transient
    public static final String FD_TIME_COST_DESC = "timeCostDesc";

    @Transient
    public static String FD_TRAVEL_MONTH = "travelMonth";

    @Transient
    public static String FD_OPEN_TIME = "openTime";

    @Transient
    public static String FD_TRAFFIC_URL = "trafficInfoUrl";

    @Transient
    public static String FD_GUIDE_URL = "guideUrl";

    @Transient
    public static String FD_TIPS_URL = "tipsUrl";

    @Transient
    public static String FD_DESC_FLAGS = "descriptionFlag";

    public Integer spotId;

    @Embedded
    public ViewSpotRatings ratings;

    /**
     * 普通攻略
     */
    public String guide;

    /**
     * AAA景区：3
     * AAAA景区：4
     */
    public Integer rankingA;

    /**
     * 建议旅游价格
     */
    public String travelMonth;

    /**
     * 建议游玩时间，单位为小时。
     */
    public Double timeCost;

    /**
     * 建议游玩时间，单位为小时。
     */
    public String timeCostDesc;

    @Transient
    private String guideUrl;

    @Transient
    private String kengdieUrl;

    public String getZhName() {
        return zhName;
    }

    public List<String> getAlias() {
        return alias;
    }

    public Double getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Double timeCost) {
        this.timeCost = timeCost;
    }

    public String getTravelMonth() {
        return travelMonth;
    }

    public String getTimeCostDesc() {
        return timeCostDesc;
    }

    public String getGuideUrl() {
        return guideUrl;
    }

    public void setGuideUrl(String guideUrl) {
        this.guideUrl = guideUrl;
    }


    public String getPriceDesc() {
        return priceDesc;
    }

    public String getTelephone() {
        return telephone;
    }


    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);

        // 按景点分类估算游玩时间
        node.put("timeCost", DataFilter.timeCostFilter(timeCost, name));

        if (level > 2) {
            if (rankingA != null)
                node.put("rankingA", rankingA);


            node.put("openTime", DataFilter.openTimeFilter(openTime));

            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
            builder.add("travelMonth", new ArrayList<>());
            for (String k : new String[]{"trafficInfo", "guide", "kengdie"}) {
                Object val;
                try {
                    val = ViewSpot.class.getField(k).get(this);
                    builder.add(k, val != null ? val : "");
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
            }
            node.putAll((ObjectNode) Json.toJson(builder.get()));
        }

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //标识描述的各项是否存在
        Map<String, Integer> flag = new HashMap<>();
        if (description != null) {
            flag.put("traffic", description.traffic == null ? 0 : 1);
            flag.put("desc", description.desc == null ? 0 : 1);
            flag.put("details", description.details == null ? 0 : 1);
            flag.put("tips", description.tips == null ? 0 : 1);
        } else {
            flag.put("traffic", 0);
            flag.put("desc", 0);
            flag.put("details", 0);
            flag.put("tips", 0);
        }
        builder.add("descriptionFlag", flag);
        node.putAll((ObjectNode) Json.toJson(builder.get()));
        return node;
    }
}
