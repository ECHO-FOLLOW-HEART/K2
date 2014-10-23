package models.backup.tag;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 用户标签。
 *
 * @author zephyre
 */
@Entity
public class UserTag extends Model {
    @Id
    public Long id;

    @Constraints.MaxLength(32)
    @Column(length = 32)
    public String userTagName;
}
