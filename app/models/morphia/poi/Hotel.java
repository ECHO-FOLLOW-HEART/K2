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

        if (level > 1 && type != null)
            node.put("type", type);
        return node;
    }
}