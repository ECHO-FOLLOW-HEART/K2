package models.geos;

import models.tag.LocalityTag;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * 城市。
 *
 * @author Zephyre
 */
@Entity
public class Locality extends Model {
    public static Finder<Long, Locality> finder = new Finder<>(Long.class, Locality.class);

    @Id
    public Long id;

    /**
     * 城市所属国家。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Country country;

    /**
     * 行政区级别：
     * 1：省
     * 2：市
     * 3：县
     */
    public Integer level;

    /**
     * 城市的英文名称。
     */
    @Constraints.Required
    public String enLocalityName;

    /**
     * 城市的中文名称。
     */
    public String zhLocalityName;

    /**
     * 城市的当地语言名称。
     */
    public String localLocalityName;

    /**
     * 城市中心纬度。
     */
    @Constraints.Min(-90)
    @Constraints.Max(90)
    @Constraints.Required
    public Float lat;

    /**
     * 城市中心经度。
     */
    @Constraints.Min(-180)
    @Constraints.Max(180)
    @Constraints.Required
    public Float lng;

    /**
     * 西南边界的纬度。
     */
    @Constraints.Min(-90)
    @Constraints.Max(90)
    public Float latSW;

    /**
     * 西南边界的经度。
     */
    @Constraints.Min(-180)
    @Constraints.Max(180)
    public Float lngSW;

    /**
     * 东北边界的纬度。
     */
    @Constraints.Min(-90)
    @Constraints.Max(90)
    public Float latNE;

    /**
     * 东北边界的经度。
     */
    @Constraints.Min(-180)
    @Constraints.Max(180)
    public Float lngNE;

    /**
     * 时区。
     */
    @Constraints.Min(-12)
    @Constraints.Max(12)
    public Integer timeZone;

    /**
     * 城市所属的标签。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public List<LocalityTag> tagList;

    /**
     * 上级管辖城市。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Locality supLocality;

    /**
     * 下级所属城市列表。
     */
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "supLocality")
    public List<Locality> chiLocalityList;

    /**
     * 城市的旅游重要性指数。
     */
    public Integer priority;

    /**
     * 百度城市代码。
     */
    public String baiduCode;

    /**
     * 去哪城市代码。
     */
    public String qunarCode;

    @Version
    public Timestamp updatedTime;
}
