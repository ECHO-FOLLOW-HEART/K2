package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import models.geo.Locality;
import models.misc.TravelNote;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("userInfoFilter")
public class UserInfo extends AizouBaseEntity implements ITravelPiFormatter {
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

    @Transient
    public static String fnAvatarSmall = "avatarSmall";

    @Transient
    public static String fnAlias = "alias";

    @Transient
    public static String fnRoles = "roles";

    @Transient
    public static String fnRoles_Common = "common";

    @Transient
    public static String fnRoles_Expert = "expert";

    @Transient
    public static String fnLevel = "level";

    @Transient
    public static String fnTravelStatus = "travelStatus";

    @Transient
    public static String fnTracks = "tracks";

    @Transient
    public static String fnTravelNotes = "travelNotes";

    @Transient
    public static String fnResidence = "residence";

    @Transient
    public static String fnBirthday = "birthday";

    @Transient
    public static String fnZodiac = "zodiac";
    /**
     * 昵称
     */
    @Constraints.Required
    @JsonProperty("nickName")
    private String nickName;
    /**
     * 头像
     */
    @JsonProperty("avatar")
    private String avatar;

    /**
     * 头像小图
     */
    @JsonProperty("avatarSmall")
    private String avatarSmall;

    /**
     * 性别： F\M\Secret\None
     */
    @JsonProperty("gender")
    private String gender;
    /**
     * 签名
     */
    @JsonProperty("signature")
    private String signature;

    /**
     * 手机号
     */
    @JsonProperty("tel")
    private String tel;

    /**
     * 国家编码
     */
    @JsonProperty("dialCode")
    private Integer dialCode;

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * 好友列表:用户ID-用户简要信息
     */
    private List<UserInfo> friends;

    /**
     * 备注信息。这个字段比较特殊：每个用户的备注信息，是由其它用户决定的，而不会跟随自身这个UserInfo存放在数据库中。
     */
    @Transient
    private String memo;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 第三方OAuth登录信息
     */
    private List<OAuthInfo> oauthList;

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
     * 环信的账号
     */
    private String easemobUser;

    /**
     * 注册来源
     */
    private String origin;

    /**
     * 别名
     */
    private List<String> alias;

    /**
     * 用户类型
     */
    private List<String> roles;

    /**
     * 用户等级
     */
    private int level;

    /**
     * 旅行状态
     */
    private String travelStatus;

    /**
     * 用户足迹
     */
    private List<Locality> tracks;

    /**
     * 用户游记
     */

    private List<TravelNote> travelNotes;

    /**
     * 居住地
     */
    private String residence;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 星座
     */
    private int zodiac;


    public UserInfo() {
    }

    public static UserInfo newInstance(Long userId) {
        UserInfo user = new UserInfo();
        user.setId(new ObjectId());
        user.setGender("U");
        user.userId = userId;
        user.nickName = "桃子_" + user.userId;
        user.roles = Arrays.asList(UserInfo.fnRoles_Common);
        user.alias = Arrays.asList(user.nickName.toLowerCase());
        user.setEnabled(true);
        return user;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Integer getDialCode() {
        return dialCode;
    }

    public void setDialCode(Integer dialCode) {
        this.dialCode = dialCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<UserInfo> getFriends() {
        return friends;
    }

    public void setFriends(List<UserInfo> friends) {
        this.friends = friends;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OAuthInfo> getOauthList() {
        return oauthList;
    }

    public void setOauthList(List<OAuthInfo> oauthList) {
        this.oauthList = oauthList;
    }

    public String getEasemobUser() {
        return easemobUser;
    }

    public void setEasemobUser(String easemobUser) {
        this.easemobUser = easemobUser;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getAvatarSmall() {
        if (avatar == null || avatar.equals(""))
            return "";
        // 如果是微信登录
        if (avatar.startsWith("http://wx.qlogo.cn"))
            return avatar;
        return String.format("%s?imageView2/2/w/%d", avatar, 200);
    }

    public void setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTravelStatus() {
        return travelStatus;
    }

    public void setTravelStatus(String travelStatus) {
        this.travelStatus = travelStatus;
    }

    public List<Locality> getTracks() {
        return tracks;
    }

    public void setTracks(List<Locality> tracks) {
        this.tracks = tracks;
    }

    public List<TravelNote> getTravelNotes() {
        return travelNotes;
    }

    public void setTravelNotes(List<TravelNote> travelNotes) {
        this.travelNotes = travelNotes;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public int getZodiac() {
        return zodiac;
    }

    public void setZodiac(int zodiac) {
        this.zodiac = zodiac;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", getId().toString()).add("nickName", nickName).add("avatar", avatar).add("secToken", "");

        return Json.toJson(builder.get());
    }
}