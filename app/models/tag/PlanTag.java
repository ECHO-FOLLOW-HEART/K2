package models.tag;

import models.plans.PlanEntry;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * 路线的标签。
 *
 * @author Zephyre
 */
@Entity
public class PlanTag extends Model {
    public static Finder<Long, PlanTag> finder = new Finder<>(Long.class, PlanTag.class);

    @Id
    public Long id;

    /**
     * 标签名称。
     */
    @Constraints.Required
    public String planTagName;

    @ManyToMany(mappedBy = "planTagList", fetch = FetchType.LAZY)
    public List<PlanEntry> planEntryList;
}
