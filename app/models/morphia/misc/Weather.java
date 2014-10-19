package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 天气
 *
 * @author Zephyre
 */
@Entity
public class Weather extends TravelPiBaseItem implements ITravelPiFormatter {

    /**
     * 地点
     */
    @Embedded
    public SimpleRef loc;

    /**
     * 当前天气
     */
    @Embedded
    public WeatherItem current;

    /**
     * 预报
     */
    public List<WeatherItem> forecast;

    /**
     * 更新时间
     */
    public Date updateTime;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();  //创建builder填充数据
        builder.add("_id", id.toString());      //文档id

        DateFormat formattime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        builder.add("updateTime", formattime.format(updateTime));

        if (loc != null) {
            builder.add("loc", loc.toJson());
        } else
            builder.add("loc", new BasicDBObject());

        if (current != null) {
            builder.add("current", current.toJson());
        } else
            builder.add("current", new BasicDBObject());

        List<JsonNode> nodelist = new ArrayList<>(4);
        if (forecast != null && !(forecast.isEmpty())) {
            for (WeatherItem item : forecast) {
                nodelist.add(item.toJson());
            }
            builder.add("forecast", nodelist);
        } else
            builder.add("forecast", new BasicDBObject());

        return Json.toJson(builder.get());

    }
}
