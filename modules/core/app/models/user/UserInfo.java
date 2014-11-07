package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

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

    @Transient
    public static String fnOauthId = "oauthList.oauthId";

    private UserInfo() {
    }

    public static UserInfo newInstance(Integer userId) {
        UserInfo user = new UserInfo();
        user.id = new ObjectId();
        user.userId = userId;
        user.nickName = "桃子_" + user.userId;
        user.enabled = true;

        return user;
    }

    /**
     * 昵称
     */
    @Constraints.Required
    private String nickName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别： F\M
     */
    private String gender;

    /**
     * 签名
     */
    private String signature;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 国家编码
     */
    private Integer dialCode;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 好友列表:用户ID-用户简要信息
     */
    private List<UserInfo> friends;

    /**
     * 备注信息。这个字段比较特殊：每个用户的备注信息，是由其它用户决定的，而不会跟随自身这个UserInfo存放在数据库中。
     */
    @Transient
    private String memo;

//    /**
//     * 好友备注:用户ID-备注信息
//     */
//    public Map<Integer, String> remark;
//
//    /**
//     * 黑名单：用户ID-用户简要信息
//     */
//    public Map<Integer, UserInfo> blackList;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 第三方OAuth登录信息
     */
    private List<OAuthInfo> oauthList;

    /**
     * 环信的账号
     */
    private String easemobUser;

    /**
     * 注册来源
     */
    private String origin;

//    /**
//     * 唯一设备号。
//     */
//    public String udid;

//    /**
//     * 用户App版本
//     *
//     * @return
//     */
//    public String appVersion;

    public String getGender() {
        return (gender != null ? gender : "");
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return (signature != null ? signature : "");
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTel() {
        return (tel != null ? tel : "");
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public List<UserInfo> getFriends() {
        return friends != null ? friends : new ArrayList<UserInfo>();
    }

    public void setFriends(List<UserInfo> friends) {
        this.friends = friends;
    }

    public Integer getDialCode() {
        return (dialCode != null ? dialCode : 86);
    }

    public void setDialCode(Integer dialCode) {
        this.dialCode = dialCode;
    }

    public String getMemo() {
        return (memo != null ? memo : "");
    }

    public String getEmail() {
        return (email != null ? email : "");
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OAuthInfo> getOauthList() {
        return oauthList != null ? oauthList : new ArrayList<OAuthInfo>();
    }

    public void setOauthList(List<OAuthInfo> oauthList) {
        this.oauthList = oauthList;
    }

    public String getNickName() {
        return nickName != null ? nickName : "";
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

//    public void setMemo(String memo) {
//        this.memo = memo;
//    }
//
//    public Map<Integer, String> getRemark() {
//        return remark != null ? remark : new HashMap<Integer, String>();
//    }
//
//    public void setRemark(Map<Integer, String> remark) {
//        this.remark = remark;
//    }
//
//    public Map<Integer, UserInfo> getBlackList() {
//        return blackList;
//    }
//
//    public void setBlackList(Map<Integer, UserInfo> blackList) {
//        this.blackList = blackList;
//    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getEasemobUser() {
        return (easemobUser != null ? easemobUser : "");
    }

    public void setEasemobUser(String easemobUser) {
        this.easemobUser = easemobUser;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("nickName", nickName).add("avatar", avatar).add("secToken", "");

        return Json.toJson(builder.get());
    }
}