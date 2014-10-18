package models.morphia.misc;

import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;

/**
 * 单项天气信息
 *
 * @author Zephyre
 */
@Embedded
public class WeatherItem {
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
}
