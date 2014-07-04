package models.geos;

import play.data.validation.Constraints;

import javax.persistence.Embeddable;

/**
 * 地址信息
 *
 * @author Zephyre
 */
@Embeddable
public class Address {
    /**
     * 所属地区。
     */
    public String district;

    /**
     * 详细街道信息。
     */
    public String thoroughfares;

    /**
     * 格式化地址字符串。
     */
    public String formattedAddr;

    /**
     * 纬度
     */
    @Constraints.Max(90)
    @Constraints.Min(-90)
    public Float lat;

    /**
     * 经度
     */
    @Constraints.Max(180)
    @Constraints.Min(-180)
    public Float lng;
}
