package models.geo;


import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseItem;
import models.ITravelPiFormatter;
import models.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

/**
 * 地址。
 *
 * @author Zephyre
 */
@Embedded
@JsonFilter("addressFilter")
public class Address extends AizouBaseItem implements ITravelPiFormatter {
    @Transient
    public static String simpAddress = "address";

    @Transient
    public static String simpLoc = "loc";

    @Transient
    public static String simpCoords = "coords";

    @Transient
    public static String simpBCoords = "bCoords";

    public String address;

    @Embedded
    public SimpleRef loc;

    @Embedded
    public Coords coords;

    @Embedded
    public Coords bCoords;

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
        // TODO
        builder.add("locName", (locName != null ? locName : ""));
//        builder.add("locName", (locName != null ? Locality.stripLocName(locName) : ""));

        if (level > 1) {
            builder.add("addr", (address != null ? address : ""));

            if (level > 2) {
                for (String k : new String[]{"lat", "lng"}) {
                    Coords tmpCoords = bCoords != null ? bCoords : coords;
                    if (tmpCoords != null) {
                        Object val = null;
                        try {
                            val = Coords.class.getField(k).get(tmpCoords);
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
