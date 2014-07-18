package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 景点信息。
 *
 * @author Zephyre
 */
public class ViewSpot extends AbstractPOI {
//    @Id
//    public ObjectId id;
//
//    @Embedded
//    public Ratings ratings;
//
//    @Embedded
//    public Contact contact;
//
//    @Embedded
//    public Address addr;
//
//    public String name;
//
//    public String url;
//
//    public Double price;
//
//    public String priceDesc;
//
//    public String desc;
//
//    public List<String> imageList;
//
//    public List<String> tags;
//
//    public List<String> alias;

    public Boolean worldHeritage;

    public Integer spotId;

    public List<Integer> travelMonth;

    public String trafficInfo;

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

    public String openTime;

    /**
     * 建议游玩时间，单位为小时。
     */
    public Double timeCost;


    @Override
    public JsonNode toJson() {
        return toJson(3);
    }

    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);
        node.put("timeCost", (timeCost != null && timeCost > 0) ? timeCost : 3);

        if (level > 2) {
            if (rankingA != null)
                node.put("rankingA", rankingA);
            //PC_Chen:
//            else
//                node.put("rankingA", "");

            node.put("openTime", openTime != null ? openTime : "");

            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
            for (String k : new String[]{"travelMonth", "trafficInfo", "guide", "kengdie"}) {
                Object val = null;
                try {
                    val = ViewSpot.class.getField(k).get(this);
                    //PC_Chen , travelMonth is a list
                    if(k.equals("travelMonth")){
                        builder.add(k, val != null ? val : new ArrayList<>());
                    }else
                        builder.add(k, val != null ? val : "");
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
            }
            node.putAll((ObjectNode) Json.toJson(builder.get()));
        }
        return node;
    }
}
