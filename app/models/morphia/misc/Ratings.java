package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * POI的评论信息。
 *
 * @author Zephyre
 */
@Embedded
public class Ratings implements ITravelPiFormatter {
    public Integer score;

    public Integer viewCnt;

    public Integer favorCnt;

    public Integer dinningIdx;

    public Integer shoppingIdx;

    public Double ranking;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // TODO hardcode here
//        if (ranking == null)
//            ranking = 0.8;
        if (viewCnt == null)
            viewCnt = 0;
        if (favorCnt == null)
            favorCnt = 0;

        for (String k : new String[]{"score", "dinningIdx", "shoppingIdx", "viewCnt", "favorCnt", "ranking"}) {
            Object val = null;
            try {
                val = Ratings.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            if (val != null)
                builder.add(k, val);
        }


        return Json.toJson(builder.get());
    }
}
