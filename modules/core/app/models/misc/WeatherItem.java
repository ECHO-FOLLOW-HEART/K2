package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.data.validation.Constraints;
import play.libs.Json;

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
     * 详细天气描述
     */
    public String details;

    /**
     * 天气代码
     */
    @Constraints.Required
    public Integer code;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("code", code)
                .add("desc", (desc != null ? desc : ""))
                .add("details", (details != null ? details : ""))
                .add("curTemp", currTemperature)
                .add("lowerTemp", lowerTemperature)
                .add("upperTemp", upperTemperature);
        return Json.toJson(builder.get());
    }
}
