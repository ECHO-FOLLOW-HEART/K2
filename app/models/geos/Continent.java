package models.geos;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * 大洲。
 *
 * @author Zephyre
 */
@Entity
public class Continent extends Model {
    @Id
    public Long id;

    /**
     * 英文名称。
     */
    @Constraints.Required
    public String enContinentName;

    /**
     * 中文名称。
     */
    @Constraints.Required
    public String zhContinentName;

    /**
     * 对应的国家列表。
     */
    @OneToMany(mappedBy = "continent", fetch = FetchType.LAZY)
    public List<Country> countryList;
}
