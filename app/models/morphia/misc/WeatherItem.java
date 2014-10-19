package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.util.Date;

/**
 * 单项天气信息
 *
 * @author Zephyre
 */
@Embedded
public class WeatherItem implements ITravelPiFormatter {
    /**
     * 最低温度
     */
    public Double lowerTemperature;

    /**
     * 最高温度
     */
    public Double upperTemperature;

    /**
     * 当前温度
     */
    public Double currTemperature;

    /**
     * 天气描述
     */
    public String desc;

    /**
     * 天气代码
     */
    public Integer code;


    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("code", code).add("desc", (desc != null ? desc : "")).
                add("currTemperature", currTemperature).
                add("lowerTemperature", lowerTemperature).
                add("upperTemperature", upperTemperature);
        return Json.toJson(builder.get());
    }
}
