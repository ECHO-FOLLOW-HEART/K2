//package controllers.thrift;
//
//import com.lvxingpai.yunkai.ChatGroupProp;
//import com.lvxingpai.yunkai.Gender;
//import com.lvxingpai.yunkai.UserInfo;
//import com.lvxingpai.yunkai.UserInfoProp;
//import com.lvxingpai.yunkai.ChatGroup;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by topy on 2015/6/14.
// */
//public class ThriftConvert {
//
//    public static models.user.UserInfo convertK2User(UserInfo userInfo) {
//        models.user.UserInfo result = new models.user.UserInfo();
//        result.setUserId(userInfo.getUserId());
//        result.setNickName(userInfo.getNickName() == null ? "旅行派用户" : userInfo.getNickName());
//        result.setAvatar(userInfo.getAvatar() == null ? "" : userInfo.getAvatar());
//        result.setGender(userInfo.getGender() == null ? "None" : convertUserGender(userInfo.getGender()));
//        result.setSignature(userInfo.getSignature() == null ? "" : userInfo.getSignature());
//        result.setTel(userInfo.getTel() == null ? "" : userInfo.getTel());
//
//        return result;
//    }
//
//    public static List<models.user.UserInfo> convertK2UserList(List<UserInfo> userInfoList) {
//        List<models.user.UserInfo> result = new ArrayList<>();
//        for (UserInfo userInfo : userInfoList) {
//            result.add(convertK2User(userInfo));
//        }
//        return result;
//    }
//
//    public static models.group.ChatGroup convertK2ChatGroup(ChatGroup chatGroup) {
//        models.group.ChatGroup result = new models.group.ChatGroup();
//        result.setGroupId(chatGroup.getChatGroupId());
//        result.setName(chatGroup.getName() == null ? "" : chatGroup.getName());
//        result.setDesc(chatGroup.getGroupDesc() == null ? "" : chatGroup.getGroupDesc());
//        result.setAvatar(chatGroup.getAvatar() == null ? "" : chatGroup.getAvatar());
//        result.setMaxUsers(chatGroup.getMaxUsers());
//        return result;
//    }
//
//    public static List<models.group.ChatGroup> convertK2ChatGroupList(List<ChatGroup> chatGroupList) {
//        List<models.group.ChatGroup> result = new ArrayList<>();
//        for (ChatGroup chatGroup : chatGroupList) {
//            result.add(convertK2ChatGroup(chatGroup));
//        }
//        return result;
//    }
//
//    public static String convertUserGender(Gender value) {
//        if (value.equals(Gender.MALE))
//            return models.user.UserInfo.fnGender_M;
//        else if (value.equals(Gender.FEMALE))
//            return models.user.UserInfo.fnGender_F;
//        else
//            return models.user.UserInfo.fnGender_None;
//    }
//
//    public static Map<UserInfoProp, String> convertUserFieldsToPropMap(Map<String, String> map) {
//        Map<UserInfoProp, String> result = new HashMap<>();
//
//        String k;
//        String v;
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            k = entry.getKey();
//            v = entry.getValue();
//            result.put(convertUserPropMap(k), v);
//        }
//        return result;
//    }
//
//    public static Map<ChatGroupProp, String> convertGroupFieldsToPropMap(Map<String, String> map) {
//        Map<ChatGroupProp, String> result = new HashMap<>();
//
//        String k;
//        String v;
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            k = entry.getKey();
//            v = entry.getValue();
//            result.put(convertChatGroupPropMap(k), v);
//        }
//        return result;
//    }
//
//    public static UserInfoProp convertUserPropMap(String str) {
//        if (str.equals(models.user.UserInfo.fnUserId))
//            return UserInfoProp.USER_ID;
//        else if (str.equals(models.user.UserInfo.fnNickName))
//            return UserInfoProp.NICK_NAME;
//        else if (str.equals(models.user.UserInfo.fnAvatar))
//            return UserInfoProp.AVATAR;
//        else if (str.equals(models.user.UserInfo.fnAvatar))
//            return UserInfoProp.AVATAR;
//        else if (str.equals(models.user.UserInfo.fnGender))
//            return UserInfoProp.GENDER;
//        else if (str.equals(models.user.UserInfo.fnSignature))
//            return UserInfoProp.SIGNATURE;
//        else if (str.equals(models.user.UserInfo.fnTel))
//            return UserInfoProp.TEL;
//        else
//            return null;
//    }
//
//    public static ChatGroupProp convertChatGroupPropMap(String str) {
//        if (str.equals(models.group.ChatGroup.FD_NAME))
//            return ChatGroupProp.NAME;
//        else if (str.equals(models.group.ChatGroup.FD_AVATAR))
//            return ChatGroupProp.AVATAR;
//        else if (str.equals(models.group.ChatGroup.FD_DESC))
//            return ChatGroupProp.GROUP_DESC;
//        else
//            return null;
//    }
//
//}
