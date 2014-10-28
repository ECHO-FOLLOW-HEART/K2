package models.user;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

/**
 * 涉及用户的秘密数据
 *
 * @author Zephyre
 */
public class Credential {

    @Id
    public ObjectId id;

    @Constraints.Required
    public Integer userId;

    public String pwdHash;

    @Constraints.Required
    public String salt;

    /**
     * 环信密码
     */
    public String easemobPwd;
}
