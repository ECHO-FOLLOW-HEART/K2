package models.morphia.geo;


import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 地址。
 *
 * @author Zephyre
 */
@Embedded
public class Address implements ITravelPiFormatter {
    public String address;

    @Embedded
    public SimpleRef loc;

    @Embedded
    public Coords coords;


    /**
     * 序列化到JSON格式。
     *
     * @param level 序列化级别：1：只有loc信息；2：loc和address信息；3：完整信息。
     * @return
     */
    public JsonNode toJson(int level) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        String locId = null, locName = null;
        if (loc != null) {
            locId = (loc.id != null ? loc.id.toString() : null);
            locName = (loc.zhName != null ? loc.zhName : null);
        }
        builder.add("locId", (locId != null ? locId : ""));
        builder.add("locName", (locName != null ? Locality.stripLocName(locName) : ""));

        if (level > 1) {
            builder.add("addr", (address != null ? address : ""));

            if (level > 2) {
                for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
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
            }
        }
        return Json.toJson(builder.get());
    }

    /**
     * 默认序列化级别：1级。
     *
     * @return
     */
    public JsonNode toJson() {
        return toJson(1);
    }
}
