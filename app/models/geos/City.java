package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import models.poi.Hotel;

import javax.persistence.*;
import java.util.List;

/**
 * 城市。
 *
 * @author Haizi
 */
@Entity
public class City extends Model {
    public static Finder<Long, City> finder = new Finder<>(Long.class, City.class);

    @Id
    public Long city;

    /**
     * 城市所属国家。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Country country;

    /**
     * 城市所属行政区。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public AdminArea adminArea;

    /**
     * 城市的英文名称。
     */
    @Constraints.Required
    public String enCityName;

    /**
     * 城市的中文名称。
     */
    public String zhCityName;

    /**
     * 城市的当地语言名称。
     */
    public String localCityName;

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
    public List<CityTag> tagList;

//    /**
//     * 城市的酒店列表。
//     */
//    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
//    public List<Hotel> hotelList;

    public Integer priority;

    /**
     * 百度城市代码。
     */
    public String baiduCode;
}
