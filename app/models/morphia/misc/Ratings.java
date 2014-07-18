package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * POI的评论信息。
 *
 * @author Zephyre
 */
@Embedded
public class Ratings {
    public Integer score;

    public Integer viewCnt;

    public Integer favorCnt;

    public Integer dinningIdx;

    public Integer shoppingIdx;

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        for (String k : new String[]{"score", "dinningIdx", "shoppingIdx", "viewCnt", "favorCnt"}) {
            try {
                Object val = Ratings.class.getField(k).get(this);
                if (k.equals("viewCnt") || k.equals("favorCnt") || k.equals("score"))
                    //PC_Chen
                    if (val != null) builder.add(k, val);
//                    builder.add(k, val != null ? val : 0);
//                else
//                    builder.add(k, val != null ? val : "");
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }
        return Json.toJson(builder.get());
    }
}
