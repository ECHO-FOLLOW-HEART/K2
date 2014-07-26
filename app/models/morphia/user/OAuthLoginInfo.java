package models.morphia.user;

import org.mongodb.morphia.annotations.Embedded;
import play.data.validation.Constraints;

/**
 * 第三方OAuth登录信息。
 * Created by zephyre on 7/24/14.
 */
@Embedded
public class OAuthLoginInfo {
    @Constraints.Required
    public String provider;

    @Constraints.Required
    public String oauthId;

    @Constraints.Required
    public String nickName;

    public String avatar;

    public String token;
}
