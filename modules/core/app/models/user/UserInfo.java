package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("userInfoFilter")
public class UserInfo extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static String fnContacts = "friends";

    @Transient
    public static String fnNickName = "nickName";

    @Transient
    public static String fnAvatar = "avatar";

    @Transient
    public static String fnGender = "gender";

    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnSignature = "signature";

    @Transient
    public static String fnTel = "tel";

    @Transient
    public static String fnDialCode = "dialCode";

    @Transient
    public static String fnEmail = "email";

    @Transient
    public static String fnMemo = "memo";

    @Transient
    public static String fnEasemobUser = "easemobUser";

    /**
     * 昵称
     */
    @Constraints.Required
    public String nickName;

    /**
     * 头像
     */
    @Constraints.Required
    public String avatar;

    /**
     * 性别： F\M
     */
    public String gender;
    /**
     * 签名
     */
    public String signature;
    /**
     * 手机号
     */
    public String tel;
    /**
     * 国家编码
     */
    public Integer dialCode;
    /**
     * 用户ID
     */
    public Integer userId;
    /**
     * 好友列表:用户ID-用户简要信息
     */
    public List<UserInfo> friends;
    /**
     * 备注信息。这个字段比较特殊：每个用户的备注信息，是由其它用户决定的，而不会跟随自身这个UserInfo存放在数据库中。
     */
    @Transient
    public String memo;
    /**
     * 好友备注:用户ID-备注信息
     */
    public Map<Integer, String> remark;
    /**
     * 黑名单：用户ID-用户简要信息
     */
    public Map<Integer, UserInfo> blackList;
    /**
     * 邮箱
     */
    public String email;
    /**
     * 第三方OAuth登录信息
     */
    public List<OAuthInfo> oauthList;
    /**
     * 环信的账号
     */
    public String easemobUser;
    /**
     * 来源
     */
    public String origin;
    /**
     * 唯一设备号。
     */
    public String udid;
    /**
     * 用户手机系统及版本
     *
     * @return
     */
    public String platform;
    /**
     * 用户App版本
     *
     * @return
     */
    public String appVersion;

//    /**
//     * 用户令牌
//     */
//    public String secToken;

    public String getGender() {
        return (gender != null ? gender : "");
    }

    public String getSignature() {
        return (signature != null ? signature : "");
    }

    public String getTel() {
        return (tel != null ? tel : "");
    }

    public List<UserInfo> getFriends() {
        if (friends == null)
            return new ArrayList<>();
        else
            return friends;
    }

    public String getMemo() {
        return (memo != null ? memo : "");
    }

    public String getEmail() {
        return (email != null ? email : "");
    }

    public List<OAuthInfo> getOauthList() {
        if (oauthList == null)
            return new ArrayList<>();
        else
            return oauthList;
    }

    public String getEasemobUser() {
        return (easemobUser != null ? easemobUser : "");
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("nickName", nickName).add("avatar", avatar).add("secToken", "");

        return Json.toJson(builder.get());
    }
}