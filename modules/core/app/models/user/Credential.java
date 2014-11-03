package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

/**
 * 涉及用户的秘密数据
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("credentialFilter")
public class Credential extends TravelPiBaseItem {
    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnPwdHash = "pwdHash";

    @Transient
    public static String fnSalt = "salt";

    @Transient
    public static String fnSecKey = "secKey";

    @Transient
    public static String fnEasemobPwd = "easemobPwd";

    @Constraints.Required
    public Integer userId;

    public String pwdHash;

    @Constraints.Required
    public String salt;

    /**
     * 环信密码
     */
    public String easemobPwd;

    /**
     * 用户密钥
     */
    @Constraints.Required
    public String secKey;
}
