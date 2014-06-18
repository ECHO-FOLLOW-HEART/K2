package models.users;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 用户
 *
 * @author Haizi
 */
@Entity
public class UserEntry extends Model{
    @Id
    public Long id;

    @Constraints.Required
    public String userName;

    public String nickName;
}
