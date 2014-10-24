package models.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.misc.CheckinRatings;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 酒店使用的评价数据。
 *
 * @author Zephyre
 */
@Embedded
public class HotelRatings extends CheckinRatings {
    public Integer starLevel;

    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (starLevel != null)
            node.put("starLevel", starLevel);
        return node;
    }
}
