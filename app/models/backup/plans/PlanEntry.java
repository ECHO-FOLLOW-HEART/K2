package models.backup.plans;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import models.backup.tag.PlanTag;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.sql.Timestamp;
import java.util.List;

/**
 * 旅游路线信息。
 *
 * @author Zephyre
 */
@Entity
public class PlanEntry extends Model {
    public static Finder<Long, PlanEntry> finder = new Finder<>(Long.class, PlanEntry.class);
    @Id
    public Long id;
    /**
     * 路线的类型。
     */
    public PlanType planType = PlanType.PREDEFINED;
    /**
     * 路线的标签。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public List<PlanTag> planTagList;
    /**
     * 路线持续的天数。
     */
    public Integer duration = 1;
    /**
     * 路线的标题。
     */
    public String title;
    /**
     * 路线描述。
     */
    public String description;
    /**
     * 路线的封面图像。
     */
    public String coverImage;
    /**
     * 路线预计费用的下限。
     */
    public Integer minPrice;
    /**
     * 路线预计费用的上限。
     */
    public Integer maxPrice;
    /**
     * 美食指数。
     */
    public Integer foodIdx;
    /**
     * 购物指数。
     */
    public Integer shoppingIdx;
    /**
     * 风景指数。
     */
    public Integer sightSeeingIdx;
    @CreatedTimestamp
    public Timestamp createdTime;
    @UpdatedTimestamp
    public Timestamp updatedTime;

    /**
     * 表示路线的类型。
     */
    public enum PlanType {
        /**
         * 系统预设的路线。
         */
        @EnumValue("1")
        PREDEFINED,
        /**
         * 用户自定义的路线。
         */
        @EnumValue("2")
        USER_GENERATED
    }
}
