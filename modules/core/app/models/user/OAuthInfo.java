package models.user;

import org.mongodb.morphia.annotations.Embedded;

import javax.validation.constraints.NotNull;

/**
 * 第三方OAuth登录信息。
 * Created by zephyre on 7/24/14.
 */
@Embedded
public class OAuthInfo {
    /**
     * 第三方账号体系的名称。比如：weixin表示这是微信账号
     */
    @NotNull
    private String provider;

    /**
     * 用户在第三方账号体系中的id
     */
    @NotNull
    private String oauthId;

    /**
     * 用户在第三方账号的昵称
     */
    @NotNull
    private String nickName;

    /**
     * 用户在第三方账号的头像
     */
    private String avatar;

    /**
     * 用户在第三方账号的token
     */
    private String token;

    public String getProvider() {
        return provider != null ? provider : "";
    }

    public void setProvider(String val) {
        provider = val;
    }

    public String getOauthId() {
        return oauthId != null ? oauthId : "";
    }

    public void setOauthId(String val) {
        oauthId = val;
    }

    public String getNickName() {
        return nickName != null ? nickName : "";
    }

    public void setNickName(String val) {
        nickName = val;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "";
    }

    public void setAvatar(String val) {
        avatar = val;
    }

    public String getToken() {
        return token != null ? token : "";
    }

    public void setToken(String val) {
        token = val;
    }

}
