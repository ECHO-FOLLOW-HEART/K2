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
public class Continent extends Model{
    @Id
    public Long id;

    @Constraints.Required
    public String enContinentName;

    @Constraints.Required
    public String zhContinentName;

    @OneToMany(mappedBy = "continent", fetch = FetchType.LAZY)
    public List<Country> countryList;

    public Integer test1;

}
