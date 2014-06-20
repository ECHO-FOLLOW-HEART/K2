package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * 国家。
 *
 * @author Zephyre
 */
@Entity
public class Country extends Model {
    public static Finder<String, Country> finder = new Finder<>(String.class, Country.class);

    /**
     * 基于ISO 3166-1 alpha-2标准的国家代码。
     */
    @Id
    @Constraints.MaxLength(value = 2)
    public String countryCode2;

    /**
     * 基于ISO 3166-1 alpha-3标准的国家代码。
     */
    @Constraints.MaxLength(value = 3)
    public String countryCode3;

    /**
     * 电话号码的国家代码。
     */
    @Constraints.Min(1)
    public Integer telCode;

    /**
     * 国家的英文名称。
     */
    @Constraints.Required
    public String enCountryName;

    /**
     * 国家的中文名称。
     */
    @Constraints.Required
    public String zhCountryName;

    /**
     * 属于哪个大洲。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Continent continent;

    /**
     * 属于哪个区域。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public SubContinent subContinent;

    /**
     * 下属城市列表。
     */
    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    public List<Locality> localityList;
}
