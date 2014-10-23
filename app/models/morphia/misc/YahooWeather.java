package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import play.data.validation.Constraints;
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
public class YahooWeather extends TravelPiBaseItem implements ITravelPiFormatter {

    /**
     * 地点
     */
    @Embedded
    @Constraints.Required
    public SimpleRef loc;

    /**
     * 当前天气
     */
    @Embedded
    @Constraints.Required
    public WeatherItem current;

    /**
     * 预报
     */
    public List<WeatherItem> forecast;

    /**
     * 更新时间
     */
    @Constraints.Required
    public Date updateTime;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();  //创建builder填充数据

        DateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        builder.add("updateTime", timeformat.format(updateTime));

        builder.add("enLocName", (loc.enName != null ? loc.enName : ""))
                .add("zhLocName", (loc.zhName != null ? loc.zhName : ""));

        builder.add("current", current.toJson());

        List<JsonNode> nodelist = new ArrayList<>();
        if (forecast != null) {
            for (WeatherItem item : forecast)
                nodelist.add(item.toJson());
        }
        builder.add("forecast", nodelist);

        return Json.toJson(builder.get());
    }
}
