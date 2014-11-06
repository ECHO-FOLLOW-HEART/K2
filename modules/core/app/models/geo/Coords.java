package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.utils.IndexDirection;
import play.libs.Json;

/**
 * 坐标类。
 *
 * @author Zephyre
 */

@Embedded
@JsonFilter("coordsFilter")
public class Coords extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static String simpLat = "lat";
    @Transient
    public static String simpLng = "lng";
    @Transient
    public static String simpId="id";

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
