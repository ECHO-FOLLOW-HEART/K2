package models.geo;

import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2015/3/24.
 */
@Embedded
public class RmdLocality extends AizouBaseEntity {

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_EN_NAME = "enName";

    @Transient
    public static final String FD_EN_PINYIN = "pinyin";

    @Transient
    public static final String FD_EN_PROVINCE = "province";

    /**
     * 中文名称
     */
    private String zhName;
    /**
     * 英文名称
     */
    private String enName;
    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 省份
     */
    private String province;

    /**
     * 省份
     */
    private String provincePinyin;

    /**
     * 排序
     */
    private int sort;

    private GeoJsonPoint location;

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }


    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getProvincePinyin() {
        return provincePinyin;
    }

    public void setProvincePinyin(String provincePinyin) {
        this.provincePinyin = provincePinyin;
    }

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

}
