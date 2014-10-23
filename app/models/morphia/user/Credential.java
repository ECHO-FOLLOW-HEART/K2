package models.morphia.user;

import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

import java.util.Date;

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

    @Constraints.Required
    public String pwdHash;

    @Constraints.Required
    public String salt;

    /**
     * 环信账号
     */
    public String easemobUser;

    /**
     * 环信密码
     */
    public String easemobPwd;
}
