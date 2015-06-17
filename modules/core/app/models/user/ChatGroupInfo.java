package models.user;

/**
 * Created by lxf on 14-10-25.
 */

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

import java.util.List;

@Entity
public class ChatGroupInfo extends AizouBaseEntity {
    @Transient
    public static final String OWNER = "owner";
    @Transient
    public static final String MEMBERS = "members";
    @Transient
    public static final String GROUPNAME = "groupName";
    @Transient
    public static final String DESC = "desc";
    @Transient
    public static final String ISGROUPPUBLIC = "isGroupPublic";

    @Id
    public ObjectId id;

    /**
     * 群的id
     */
    @Constraints.Required
    public String groupId;

    /**
     * 群主
     */
    @Constraints.Required
    @Embedded
    public UserInfo owner;

    /**
     * 群成员
     */
    public List<UserInfo> members;

    /**
     * 群名称
     */
    public String groupName;

    /**
     * 群描述
     */
    public String desc;

    /**
     * 群是否为公开群
     */
    public Boolean isGroupPublic;

    /**
     * 群组的最大成员数
     */
    public Integer maxUsers;


}
