package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

/**
 * 涉及用户的秘密数据
 *
 * @author Zephyre
 */
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

    /**
     * 用户ID
     */
    @Constraints.Required
    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer val) {
        userId = val;
    }

    /**
     * 密码加盐hash
     */
    private String pwdHash;

    public String getPwdHash() {
        return (pwdHash != null ? pwdHash : "");
    }

    public void setPwdHash(String val) {
        pwdHash = val;
    }

    /**
     * 该用户对应的盐值
     */
    @Constraints.Required
    private String salt;

    public String getSalt() {
        return (salt != null ? salt : "");
    }

    public void setSalt(String val) {
        salt = val;
    }

    /**
     * 环信密码
     */
    private String easemobPwd;

    public String getEasemobPwd() {
        return (easemobPwd != null ? easemobPwd : "");
    }

    public void setEasemobPwd(String val) {
        easemobPwd = val;
    }

    /**
     * 用户密钥，鉴权的时候使用
     */
    @Constraints.Required
    private String secKey;

    public String getSecKey() {
        return (secKey != null ? secKey : "");
    }

    public void setSecKey(String val) {
        secKey = val;
    }
}
