package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import models.ITravelPiFormatter;
import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 路线规划中的基本单元。
 *
 * @author Zephyre
 */
@Embedded
public class PlanItem implements ITravelPiFormatter {
    @Embedded
    public SimpleRef item;

    @Embedded
    public SimpleRef loc;

    public Integer idx;

    public String type;

    public String subType;

    public Date ts;

    @Transient
    public Object extra;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        JsonNode itemNode = item.toJson();
        builder.add("itemId", itemNode.get("_id"));
        builder.add("itemName", itemNode.get("name"));
        if (loc != null) {
            JsonNode locNode = loc.toJson();
            builder.add("locId", locNode.get("_id"));
            builder.add("locName", locNode.get("name"));
        }
        builder.add("type", type != null ? type : "");
        builder.add("subType", subType != null ? subType : "");
        if (ts != null) {
            final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            fmt.setTimeZone(tz);
            builder.add("ts", fmt.format(ts));
        } else
            builder.add("ts", "");

        return Json.toJson(builder.get());
    }
}
