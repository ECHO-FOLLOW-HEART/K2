package models.morphia.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 坐标类。
 *
 * @author Zephyre
 */
@Embedded
public class Coords {
    public Double lat;
    public Double lng;
    public Double blat;
    public Double blng;

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
            try {
                Object val = Coords.class.getField(k).get(this);
                builder.add(k, val != null ? val : "");
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                builder.add(k, "");
            }
        }
        return Json.toJson(builder.get());
    }
}
