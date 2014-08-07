package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.morphia.misc.CheckinRatings;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 酒店使用的评价数据。
 *
 * @author Zephyre
 */
@Embedded
public class ViewSpotRatings extends CheckinRatings {
    public Integer rankingA;

    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (rankingA != null)
            node.put("rankingA", rankingA);
        return node;
    }
}
