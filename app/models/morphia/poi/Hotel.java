package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;

/**
 * 酒店。
 *
 * @author Zephyre
 */
@Entity
public class Hotel extends AbstractPOI {

    @Embedded
    public HotelRatings ratings;

    /**
     * 酒店类型，比如“经济型”等等。
     */
    public Integer type;

    public static List<String> getRetrievedFields(int level) {
        List<String> fieldList = AbstractPOI.getRetrievedFields(level);
        fieldList.add("price");
        if (level > 1)
            fieldList.add("type");
        return fieldList;
    }

    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);
        if (ratings != null && ratings.starLevel != null)
            ((ObjectNode) node.get("ratings")).put("starLevel", ratings.starLevel);
        else
            ((ObjectNode) node.get("ratings")).put("starLevel", 3);
        if (null == price) {
            if (ratings != null) {
                if (ratings.starLevel == null || ratings.starLevel < 3) {
                    price = 150.0;
                } else if (ratings.starLevel == 3) {
                    price = 300.0;
                } else if (ratings.starLevel == 4) {
                    price = 400.0;
                } else if (ratings.starLevel > 5) {
                    price = 800.0;
                }
            } else {

                price = 150.0;
            }

        }
        node.put("price", price);
        if (level > 1 && type != null)
            node.put("type", type);
        return node;
    }
}