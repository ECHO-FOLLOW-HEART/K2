package controllers.thrift;

import com.aizou.yunkai.ChatGroup;
import com.aizou.yunkai.ChatGroupProp;
import com.aizou.yunkai.UserInfo;
import com.aizou.yunkai.UserInfoProp;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by topy on 2015/6/14.
 */
public class ThriftConvert {

    public static models.user.UserInfo convertK2User(UserInfo userInfo) {
        models.user.UserInfo result = new models.user.UserInfo();
        result.setUserId(userInfo.getUserId());
        result.setNickName(userInfo.getNickName());
        result.setAvatar(userInfo.getAvatar());
        result.setGender(userInfo.getGender().getValue() == 0 ? "M" : "F");
        result.setSignature(userInfo.getSignature());
        result.setTel(userInfo.getTel());

        return result;
    }

//    public static Map<String, String> convertUserPropMapToFileds(Map<UserInfoProp, String> map) {
//        Map<String, String> result = new HashMap<>();
//
//        UserInfoProp k;
//        String v;
//        for (Map.Entry<UserInfoProp, String> entry : map.entrySet()) {
//            k = entry.getKey();
//            v = entry.getValue();
//            result.put(convertUserPropMap(k), v);
//        }
//        return result;
//    }

    public static Map<UserInfoProp, String> convertUserFieldsToPropMap(Map<String, String> map) {
        Map<UserInfoProp, String> result = new HashMap<>();

        String k;
        String v;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            k = entry.getKey();
            v = entry.getValue();
            result.put(convertUserPropMap(k), v);
        }
        return result;
    }

    public static Map<ChatGroupProp, String> convertGroupFieldsToPropMap(Map<String, String> map) {
        Map<ChatGroupProp, String> result = new HashMap<>();

        String k;
        String v;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            k = entry.getKey();
            v = entry.getValue();
            result.put(convertChatGroupPropMap(k), v);
        }
        return result;
    }

//    public static String convertUserPropMap(UserInfoProp prop) {
//        switch (prop) {
//            case USER_ID:
//                return models.user.UserInfo.fnUserId;
//            case NICK_NAME:
//                return models.user.UserInfo.fnNickName;
//            case AVATAR:
//                return models.user.UserInfo.fnAvatar;
//            case GENDER:
//                return models.user.UserInfo.fnGender;
//            case SIGNATURE:
//                return models.user.UserInfo.fnSignature;
//            case TEL:
//                return models.user.UserInfo.fnTel;
//            default:
//                return null;
//        }
//    }

    public static UserInfoProp convertUserPropMap(String str) {
        if (str.equals(models.user.UserInfo.fnUserId))
            return UserInfoProp.USER_ID;
        else if (str.equals(models.user.UserInfo.fnNickName))
            return UserInfoProp.NICK_NAME;
        else if (str.equals(models.user.UserInfo.fnAvatar))
            return UserInfoProp.AVATAR;
        else if (str.equals(models.user.UserInfo.fnAvatar))
            return UserInfoProp.AVATAR;
        else if (str.equals(models.user.UserInfo.fnGender))
            return UserInfoProp.GENDER;
        else if (str.equals(models.user.UserInfo.fnSignature))
            return UserInfoProp.SIGNATURE;
        else if (str.equals(models.user.UserInfo.fnTel))
            return UserInfoProp.TEL;
        else
            return null;
    }

    public static ChatGroupProp convertChatGroupPropMap(String str) {
        if (str.equals(models.group.ChatGroup.FD_NAME))
            return ChatGroupProp.NAME;
        else if (str.equals(models.group.ChatGroup.FD_AVATAR))
            return ChatGroupProp.AVATAR;
        else if (str.equals(models.group.ChatGroup.FD_AVATAR))
            return ChatGroupProp.AVATAR;
        else
            return null;
    }

}
