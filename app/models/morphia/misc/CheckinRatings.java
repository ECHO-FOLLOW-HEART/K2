package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.morphia.misc.Ratings;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 支持签到统计的评价信息。
 *
 * @author Zephyre
 */
@Embedded
public class CheckinRatings extends Ratings {
    public Integer checkinCnt;

    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        node.put("checkinCnt", (checkinCnt != null && checkinCnt > 0) ? checkinCnt : 0);
        return node;
    }
}
