package models.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;
import java.util.Map;

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
     * 星级
     */
    private Long starLevel;

    /**
     * 房间价目表
     */
    private Map<String, Float> priceTable;

    /**
     * 酒店类型，比如“经济型”等等。
     */
    private String hotelType;

    public static List<String> getRetrievedFields(int level) {
        List<String> fieldList = AbstractPOI.getRetrievedFields(level);
        fieldList.add("price");
        if (level > 1)
            fieldList.add("hotelType");
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
        if (level > 1 && hotelType != null)
            node.put("hotelType", hotelType);
        return node;
    }
}