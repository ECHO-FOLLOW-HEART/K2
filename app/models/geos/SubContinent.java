package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * 洲际地区，如中东、东南亚、南亚等。
 *
 * @author Haizi
 */
@Entity
public class SubContinent extends Model{
    @Id
    public Long id;

    /**
     * 英文名称。
     */
    @Constraints.Required
    public String enSubContinentName;

    /**
     * 中文名称。
     */
    @Constraints.Required
    public String zhSubContinentName;

    /**
     * 对应的国家列表。
     */
    @OneToMany(mappedBy = "subContinent", fetch = FetchType.LAZY)
    public List<Country> countryList;
}
