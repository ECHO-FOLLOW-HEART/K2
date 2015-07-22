package models.geo;

import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 国家与达人的符合信息
 *
 * @author topy
 */
@Entity
public class CountryExpert extends AizouBaseEntity {
    @Transient
    public static final String FD_ZH_NAME = "zhName";
    @Transient
    public static final String FD_EN_NAME = "enName";
    @Transient
    public static String FD_CODE = "code";

    @Transient
    public static String fnImages = "images";

    /**
     * 中文名称
     */
    private String zhName;
    /**
     * 英文名称
     */
    private String enName;

    /**
     * 图片
     */
    private List<ImageItem> images;

    private String code;

    private Integer expertCnt;

    private Continent continent;

    private Integer rank;

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

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getExpertCnt() {
        return expertCnt;
    }

    public void setExpertCnt(Integer expertCnt) {
        this.expertCnt = expertCnt;
    }

    public Continent getContinent() {
        return continent;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
