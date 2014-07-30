package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 支持签到统计的评价信息。
 *
 * @author Zephyre
 */
@Embedded
public class CheckinRatings extends Ratings implements ITravelPiFormatter {
    public Integer checkinCnt;

    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (checkinCnt == null)
            checkinCnt = (score != null ? score * 9 % 122 : 24);//(new Random()).nextInt(324);
        // TODO hardcode
        node.put("checkinCnt", checkinCnt);
        return node;
    }
}
