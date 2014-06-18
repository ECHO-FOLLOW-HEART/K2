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
 * @author Haizi
 */
@Entity
public class SubContinent extends Model{
    @Id
    public Long id;

    @Constraints.Required
    public String enSubContinentName;

    @Constraints.Required
    public String zhSubContinentName;

    @OneToMany(mappedBy = "subContinent", fetch = FetchType.LAZY)
    public List<Country> countryList;
}
