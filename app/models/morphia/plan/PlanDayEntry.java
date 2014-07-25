package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 路线规划中，每一天的数据。
 *
 * @author Zephyre
 */
@Embedded
public class PlanDayEntry implements ITravelPiFormatter {
    public Date date;

    @Embedded
    public List<PlanItem> actv;

    public PlanDayEntry(Date date) {
        // 设置为当日的零点
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.setTime(date);
        tmpCal.set(Calendar.HOUR_OF_DAY, 0);
        tmpCal.set(Calendar.MINUTE, 0);
        tmpCal.set(Calendar.SECOND, 0);
        tmpCal.set(Calendar.MILLISECOND, 0);
        this.date = tmpCal.getTime();
        actv = new ArrayList<>();
    }

    public PlanDayEntry() {
    }

    public PlanDayEntry(Calendar cal) {
        // 设置为当日的零点
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.setTimeInMillis(cal.getTimeInMillis());
        tmpCal.set(Calendar.HOUR_OF_DAY, 0);
        tmpCal.set(Calendar.MINUTE, 0);
        tmpCal.set(Calendar.SECOND, 0);
        tmpCal.set(Calendar.MILLISECOND, 0);
        this.date = tmpCal.getTime();
        actv = new ArrayList<>();
    }

    public PlanDayEntry(Date date, List<PlanItem> actv) {
        this(date);
        this.actv = actv;
    }

    public PlanDayEntry(Calendar cal, List<PlanItem> actv) {
        this(cal);
        this.actv = actv;
    }

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
