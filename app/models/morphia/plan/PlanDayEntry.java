package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 路线规划中，每一天的数据。
 *
 * @author Zephyre
 */
@Embedded
public class PlanDayEntry extends TravelPiBaseItem {
    public Date date;

    @Embedded
    public List<PlanItem> actv;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        if (date != null) {
            final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            fmt.setTimeZone(tz);
            builder.add("date", fmt.format(date));
        } else
            builder.add("date", "");

        List<JsonNode> actvList = new ArrayList<>();
        if (actv != null) {
            for (PlanItem item : actv)
                actvList.add(item.toJson());
        }

        if (!actvList.isEmpty())
            builder.add("actv", Json.toJson(actvList));
        else
        //PC_Chen:
            builder.add("actv", Json.toJson(new ArrayList<>()));

        return Json.toJson(builder.get());
    }
}
