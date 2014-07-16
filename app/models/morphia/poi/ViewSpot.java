package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    /**
     * AAA景区：3
     * AAAA景区：4
     */
    public Integer rankingA;

    public String openTime;

    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (rankingA != null)
            node.put("rankingA", rankingA);
        else
            node.put("rankingA", "");
        node.put("openTime", openTime != null ? openTime : "");
        return node;
    }
}
