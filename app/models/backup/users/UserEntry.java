package models.backup.users;

import com.avaje.ebean.annotation.UpdatedTimestamp;
import models.backup.tag.UserTag;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * 用户
 *
 * @author Zephyre
 */
@Entity
public class UserEntry extends Model {
    @Id
    public Long id;

    @Constraints.Required
    public String userName;

    public String nickName;

    /**
     * 性别。
     */
    @Constraints.MaxLength(1)
    @Column(length = 1)
    public String gender;

    /**
     * 头像。
     */
    public String avatarImage;

    /**
     * 用户标签。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public List<UserTag> userTagList;

    //    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;

    public String oauthToken;
    public String oauthProvider;
    public String oauthId;
    public String oauthIdStr;
}
