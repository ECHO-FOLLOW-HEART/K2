package models.geo;

import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2015/3/24.
 */
@Embedded
public class RmdProvince extends AizouBaseEntity {

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_EN_NAME = "enName";


    @Transient
    public static final String FD_EN_PINYIN = "pinyin";

    @Transient
    public static final String FD_EN_DESTINATION = "destinations";

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
     * 目的地
     */
    private List<RmdLocality> destinations;

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

    public List<RmdLocality> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<RmdLocality> destinations) {
        this.destinations = destinations;
    }



}
