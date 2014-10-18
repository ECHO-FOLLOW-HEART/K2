package utils.DataConvert;

import com.fasterxml.jackson.databind.JsonNode;
import core.UserAPI;
import exception.TravelPiException;
import models.morphia.user.OAuthInfo;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import utils.Utils;

import java.util.ArrayList;

/**
 * Created by topy on 2014/10/14.
 */
public class UserConvert {

    public static UserInfo oauthToUserInfoForWX(JsonNode json) throws NullPointerException, TravelPiException {
        String nickname = json.get("nickname").asText();
        String headimgurl = json.get("headimgurl").asText();
        UserInfo userInfo = new UserInfo();
        userInfo.id = new ObjectId();
        userInfo.nickName = nickname;
        userInfo.avatar = headimgurl;
        userInfo.gender = json.get("sex").asText().equals("1")?"M":"F";
        userInfo.oauthList = new ArrayList<>();
        userInfo.userId = UserAPI.getUserId();
        userInfo.secToken = Utils.getSecToken();

        OAuthInfo oauthInfo = new OAuthInfo();
        oauthInfo.provider = "weixin";
        oauthInfo.oauthId = json.get("openid").asText();
        oauthInfo.nickName = nickname;
        oauthInfo.avatar = headimgurl;
        userInfo.oauthList.add(oauthInfo);

        return userInfo;
    }
}
