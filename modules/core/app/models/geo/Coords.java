package models.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 坐标类。
 *
 * @author Zephyre
 */
@Embedded
public class Coords implements ITravelPiFormatter {
    public Double lat;
    public Double lng;
    public Double blat;
    public Double blng;

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
            try {
                Object val = Coords.class.getField(k).get(this);
                //edit by PC_Chen
                if (val != null)
                    builder.add(k, val);
//                    builder.add(k, val != null ? val : "");
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                //PC_Chen:
//                builder.add(k, "");
            }
        }
        return Json.toJson(builder.get());
    }
}
