package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * 城市的标签。
 *
 * @author Haizi
 */
@Entity
public class CityTag extends Model {
    public static Finder<Long, CityTag> finder = new Finder<>(Long.class, CityTag.class);

    @Id
    public Long id;

    /**
     * 标签名称。
     */
    @Constraints.Required
    public String tagName;

    @ManyToMany(mappedBy = "tagList",fetch = FetchType.LAZY)
    public List<City> cityList;
}
