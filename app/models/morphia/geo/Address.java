package models.morphia.geo;


import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 地址。
 *
 * @author Zephyre
 */
@Embedded
public class Address {
    public String address;

    @Embedded
    public SimpleRef loc;

    @Embedded
    public Coords coords;

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("addr", (address != null ? address : ""));
        String locId = null, locName = null;
        if (loc != null) {
            locId = (loc.id != null ? loc.id.toString() : null);
            locName = (loc.zhName != null ? loc.zhName : null);
        }
        builder.add("locId", (locId != null ? locId : ""));
        builder.add("locName", (locName != null ? locName : ""));

        for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
            //edit by PC_Chen
//            builder.add(k, "");
            if (coords != null) {
                Object val = null;
                try {
                    val = Coords.class.getField(k).get(coords);
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
                if (val != null)
                    builder.add(k, val);
            }
        }
        return Json.toJson(builder.get());
    }
}
