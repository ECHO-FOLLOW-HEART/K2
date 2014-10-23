package models.backup.tag;

import models.backup.geos.Locality;
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
 * @author Zephyre
 */
@Entity
public class LocalityTag extends Model {
    public static Finder<Long, LocalityTag> finder = new Finder<>(Long.class, LocalityTag.class);

    @Id
    public Long id;

    /**
     * 标签名称。
     */
    @Constraints.Required
    public String cityTagName;

    @ManyToMany(mappedBy = "tagList", fetch = FetchType.LAZY)
    public List<Locality> localityList;
}
