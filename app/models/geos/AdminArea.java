package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * 省一级的行政区。
 *
 * @author Haizie
 */
@Entity
public class AdminArea extends Model {
    @Id
    public Long id;

    /**
     * 行政区所属国家。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Country country;

//    /**
//     * 父行政区的id。
//     */
//    public Long parentAdmin;

    /**
     * 行政区的英文名称。
     */
    @Constraints.Required
    public String enAdminName;

    /**
     * 行政区的中文名称。
     */
    public String zhAdminName;

    /**
     * 行政区的当地语言名称。
     */
    public String localAdminName;

    /**
     * 行政区下属的城市。
     */
    @OneToMany(mappedBy = "adminArea",fetch = FetchType.LAZY)
    public List<City> cityList;
}
