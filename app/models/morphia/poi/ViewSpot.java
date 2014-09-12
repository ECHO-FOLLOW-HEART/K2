package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import models.morphia.misc.Description;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;
import utils.DataFilter;
import utils.POIUtils;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 景点信息。
 *
 * @author Zephyre
 */
public class ViewSpot extends AbstractPOI {
    public Boolean worldHeritage;

    public Integer spotId;

    public List<Integer> travelMonth;

    public String trafficInfo;

    @Embedded
    public ViewSpotRatings ratings;

    /**
     * 普通攻略
     */
    public String guide;

    /**
     * 防坑攻略
     */
    public String kengdie;

    /**
     * AAA景区：3
     * AAAA景区：4
     */
    public Integer rankingA;

    /**
     * 建议游玩时间，单位为小时。
     */
    public Double timeCost;

    /**
     * 标识Description各项是否存在
     */
    public Description descriptionFlag;

    public static List<String> getRetrievedFields(int level) {
        List<String> fieldList = AbstractPOI.getRetrievedFields(level);
        if (level > 2)
            fieldList.addAll(Arrays.asList("rankingA", "openTime", "timeCost"));
        return fieldList;
    }


    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);

        // 按景点分类估算游玩时间
        node.put("timeCost", DataFilter.timeCostFilter(timeCost,name));

        if (level > 2) {
            if (rankingA != null)
                node.put("rankingA", rankingA);


            node.put("openTime", DataFilter.openTimeFilter(openTime));

            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
            for (String k : new String[]{"travelMonth", "trafficInfo", "guide", "kengdie"}) {
                Object val;
                try {
                    val = ViewSpot.class.getField(k).get(this);
                    //PC_Chen , travelMonth is a list
                    if (k.equals("travelMonth")) {
                        Collection monthList = (Collection) val;
                        builder.add(k, (monthList != null && !monthList.isEmpty()) ? monthList : new ArrayList<>());
                    } else
                        builder.add(k, val != null ? val : "");
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
            }
            node.putAll((ObjectNode) Json.toJson(builder.get()));
        }

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //标识描述的各项是否存在
        Description flag = new Description();
        if (description != null) {
            flag.traffic = description.traffic == null ? "0" : "1";
            flag.desc = description.desc == null ? "0" : "1";
            flag.details = description.details == null ? "0" : "1";
            flag.tips = description.tips == null ? "0" : "1";
        } else {
            flag.traffic = "0";
            flag.desc = "0";
            flag.details = "0";
            flag.tips = "0";
        }
        builder.add("descriptionFlag", flag);
        //builder.add("description", description == null ? "" : description.toJson());

        node.putAll((ObjectNode) Json.toJson(builder.get()));
        return node;
    }
}
