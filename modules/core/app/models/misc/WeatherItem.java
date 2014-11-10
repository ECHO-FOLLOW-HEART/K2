package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

/**
 * 单项天气信息
 *
 * @author Zephyre
 */
@Embedded
public class WeatherItem implements ITravelPiFormatter {
    @Transient
    public static String fnLowerTemperature = "lowerTemperature";
    @Transient
    public static String fnUpperTemperature = "upperTemperature";
    @Transient
    public static String fnCurrTemperature = "currTemperature";
    @Transient
    public static String fnDesc = "desc";
    @Transient
    public static String fnCode = "code";
    @Transient
    public static String fnIcon = "icon";
    @Transient
    public static String fnDetails = "details";
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

    /**
     * 天气图片
     */
    public String icon;

    public String getDetails() {
        if (details == null)
            return "";
        else
            return details;
    }

    public String getIcon() {
        if (icon == null)
            return "";
        else
            return icon;
    }

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
