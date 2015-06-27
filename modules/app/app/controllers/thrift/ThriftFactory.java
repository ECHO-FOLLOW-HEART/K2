package controllers.thrift;

import com.lvxingpai.yunkai.AuthException;
import com.lvxingpai.yunkai.ChatGroupProp;
import com.lvxingpai.yunkai.UserInfoProp;
import com.lvxingpai.yunkai.userservice;
import models.group.ChatGroup;
import models.user.UserInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.*;

/**
 * Created by topy on 2015/6/13.
 */
public class ThriftFactory {

    private static userservice.Client client = null;
    private static TTransport transport = null;

    public synchronized static userservice.Client getClient() {
        if (client == null) {
            try {
                transport = new TFramedTransport(new TSocket("192.168.100.2", 9400));
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                client = new userservice.Client(protocol);
            } catch (TTransportException e) {
            }

        }
        return client;
    }

    public static List<UserInfoProp> fields = Arrays.asList(UserInfoProp.USER_ID, UserInfoProp.NICK_NAME, UserInfoProp.AVATAR,
            UserInfoProp.GENDER, UserInfoProp.SIGNATURE, UserInfoProp.TEL, UserInfoProp.CHAT_GROUPS);

    /**
     * 取得用户信息
     *
     * @return
     * @throws TException
     */
    public static UserInfo getUserById(Long userId) {

        UserInfo u = null;
        try {
            u = ThriftConvert.convertK2User(ThriftFactory.getClient().getUserById(userId, fields));
        } catch (TException e) {
        } finally {
            // transport.close();
        }
        return u;
    }

    public static boolean existUserByTel(String tel) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.TEL, tel);
        com.lvxingpai.yunkai.UserInfo userInfo = ThriftFactory.getClient().searchUserInfo(miscInfo, Arrays.asList(UserInfoProp.USER_ID), 0, 1).get(0);
        return userInfo == null ? false : true;
    }

    public static boolean existUserByNickName(String name) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.NICK_NAME, name);
        com.lvxingpai.yunkai.UserInfo userInfo = ThriftFactory.getClient().searchUserInfo(miscInfo, Arrays.asList(UserInfoProp.USER_ID), 0, 1).get(0);
        return userInfo == null ? false : true;
    }

    public static UserInfo getUserByTel(String tel) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.TEL, tel);
        models.user.UserInfo u = ThriftConvert.convertK2User(ThriftFactory.getClient().searchUserInfo(miscInfo, fields, 0, 1).get(0));
        return u;
    }

    public static UserInfo getUserByNickName(String name) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.NICK_NAME, name);
        models.user.UserInfo u = ThriftConvert.convertK2User(ThriftFactory.getClient().searchUserInfo(miscInfo, fields, 0, 1).get(0));
        return u;
    }

    public static List<UserInfo> searchUserInfoByTelOrName(String name, int page, int pageSize) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.NICK_NAME, name);
        miscInfo.put(UserInfoProp.TEL, name);
        List<UserInfo> userList = ThriftConvert.convertK2UserList(ThriftFactory.getClient().searchUserInfo(miscInfo, fields, page, pageSize));
        return userList;
    }

    /**
     * 登录
     *
     * @param loginName
     * @param password
     * @return
     * @throws TException
     */
    public static UserInfo longin(String loginName, String password) throws TException {
        return ThriftConvert.convertK2User(ThriftFactory.getClient().login(loginName, password));
    }

    /**
     * 创建用户
     *
     * @param nickName
     * @param password
     * @param tel
     * @return
     * @throws TException
     */
    public static UserInfo createUserByTel(String nickName, String password, String tel) throws TException {
        Map<UserInfoProp, String> miscInfo = new HashMap<>();
        miscInfo.put(UserInfoProp.TEL, tel);
        return ThriftConvert.convertK2User(ThriftFactory.getClient().createUser(nickName, password, miscInfo));
    }


    /**
     * 更新用户信息
     * 支持的UserInfoProp有：nickName, signature, gender和avatar
     *
     * @param userId
     * @param userInfo
     * @throws TException
     */
    public static void updateUserInfo(long userId, Map<String, String> userInfo) throws TException {
        ThriftFactory.getClient().updateUserInfo(userId, ThriftConvert.convertUserFieldsToPropMap(userInfo));
    }

    /**
     * 更新用户手机号
     *
     * @param userId
     * @throws TException
     */
    public static void updateUserTel(long userId, String tel) throws TException {
        ThriftFactory.getClient().updateTelNumber(userId, tel);
    }

    /**
     * 修改用户密码
     *
     * @param userId
     * @param newP
     * @throws TException
     */
    public static void resetPassword(long userId, String newP) throws TException {
        ThriftFactory.getClient().resetPassword(userId, newP);
    }

    /**
     * 验证密码
     *
     * @param userId
     * @param pwd
     * @return
     * @throws TException
     */
    public static boolean verifyCredential(long userId, String pwd) throws TException {
        return ThriftFactory.getClient().verifyCredential(userId, pwd);
    }

    /**
     * 请求添加好友
     *
     * @param sender
     * @param receiver
     * @param message
     * @throws TException
     */
    public static void sendContactRequest(long sender, long receiver, String message) throws TException {
        ThriftFactory.getClient().sendContactRequest(sender, receiver, message);
    }

    /**
     * 添加好友
     *
     * @param userA
     * @param userB
     * @throws TException
     */
    public static void addContact(long userA, long userB) throws TException {
        ThriftFactory.getClient().addContact(userA, userB);
    }

    /**
     * 删除好友
     *
     * @param userA
     * @param userB
     * @throws TException
     */
    public static void removeContact(long userA, long userB) throws TException {
        ThriftFactory.getClient().removeContact(userA, userB);
    }

    public static List<UserInfo> getContactList(long userId) throws TException {
        List<UserInfoProp> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(UserInfoProp.USER_ID, UserInfoProp.NICK_NAME, UserInfoProp.TEL, UserInfoProp.AVATAR
                , UserInfoProp.GENDER, UserInfoProp.SIGNATURE));
        List<com.lvxingpai.yunkai.UserInfo> contactList = ThriftFactory.getClient().getContactList(userId, fields, 0, 1000);
        List<UserInfo> result = new ArrayList<>();

        for (com.lvxingpai.yunkai.UserInfo userInfo : contactList) {
            result.add(ThriftConvert.convertK2User(userInfo));
        }
        return result;
    }

    public static List<UserInfo> getContactIdList(long userId) throws TException {
        List<UserInfoProp> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(UserInfoProp.USER_ID));
        List<com.lvxingpai.yunkai.UserInfo> contactList = ThriftFactory.getClient().getContactList(userId, fields, 0, 1000);
        List<UserInfo> result = new ArrayList<>();

        for (com.lvxingpai.yunkai.UserInfo userInfo : contactList) {
            result.add(ThriftConvert.convertK2User(userInfo));
        }
        return result;
    }


    public static void createChatGroup(long userId, List<Long> participants, Map<String, String> groupFields) throws TException {
        Map<ChatGroupProp, String> userInfoPropStringMap = ThriftConvert.convertGroupFieldsToPropMap(groupFields);
        ThriftFactory.getClient().createChatGroup(userId, participants, userInfoPropStringMap);
    }

    public static ChatGroup getChatGroup(long gId) throws TException {
        List<ChatGroupProp> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(ChatGroupProp.NAME, ChatGroupProp.CHAT_GROUP_ID, ChatGroupProp.AVATAR, ChatGroupProp.GROUP_DESC));
        return ThriftConvert.convertK2ChatGroup(ThriftFactory.getClient().getChatGroup(gId, fields));
    }

    public static ChatGroup updateChatGroup(long gId, Map<String, String> groupFields) throws TException {
        Map<ChatGroupProp, String> userInfoPropStringMap = ThriftConvert.convertGroupFieldsToPropMap(groupFields);
        return ThriftConvert.convertK2ChatGroup(ThriftFactory.getClient().updateChatGroup(gId, userInfoPropStringMap));
    }

    public static void addChatGroupMembers(long gId, List<Long> participants) throws TException {
        ThriftFactory.getClient().addChatGroupMembers(gId, participants);
    }

    public static void removeChatGroupMembers(long gId, List<Long> participants) throws TException {
        ThriftFactory.getClient().removeChatGroupMembers(gId, participants);
    }

    public static List<UserInfo> getChatGroupMembers(long gId) throws TException {
        List<UserInfoProp> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(UserInfoProp.USER_ID, UserInfoProp.NICK_NAME, UserInfoProp.TEL, UserInfoProp.AVATAR
                , UserInfoProp.GENDER, UserInfoProp.SIGNATURE));
        List<com.lvxingpai.yunkai.UserInfo> members = ThriftFactory.getClient().getChatGroupMembers(gId, fields);
        List<UserInfo> result = new ArrayList<>();

        for (com.lvxingpai.yunkai.UserInfo userInfo : members) {
            result.add(ThriftConvert.convertK2User(userInfo));
        }
        return result;
    }

    public static List<ChatGroup> getUserChatGroups(long userId, int offset, int count) {
        try {
            List<ChatGroupProp> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(ChatGroupProp.NAME, ChatGroupProp.AVATAR, ChatGroupProp.GROUP_DESC, ChatGroupProp.CHAT_GROUP_ID));
            List<com.lvxingpai.yunkai.ChatGroup> groups = ThriftFactory.getClient().getUserChatGroups(userId, fields, offset, count);
            return ThriftConvert.convertK2ChatGroupList(groups);
        } catch (TException e) {
            return null;
        }
    }

    public static void main(String[] args) throws TException {
        System.out.print("-----");
//
//        TTransport transport = new TFramedTransport(new TSocket("192.168.100.2", 9400));
//
//        TProtocol protocol = new TBinaryProtocol(transport);
//        userservice.Client client1 = new userservice.Client(protocol);
//        transport.open();
        //UserInfo userById = ThriftFactory.getUserById(Long.valueOf(100009));
        System.out.print("--1---");
        //System.out.print(userById.getNickName());
//        Map<String, String> fieldMap = new HashMap<>();
//        fieldMap.put(ChatGroup.FD_NAME, "11");
//        fieldMap.put(ChatGroup.FD_DESC, "11");
//        fieldMap.put(ChatGroup.FD_AVATAR, "");
//        ThriftFactory.createChatGroup(3, Arrays.asList(Long.valueOf(3), Long.valueOf(100009)), fieldMap);
//        System.out.print(userChatGroups.get(0).getGroupId());


        List<UserInfo> chatGroupMembers = ThriftFactory.getChatGroupMembers(Long.valueOf(20));
        System.out.print("???");
        transport.close();

    }


}
