package controllers.thrift;

import com.aizou.yunkai.ChatGroupProp;
import com.aizou.yunkai.UserInfoProp;
import com.aizou.yunkai.userservice;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.thrift.ClientId;
import com.twitter.finagle.thrift.ThriftClientFramedCodecFactory;
import com.twitter.finagle.thrift.ThriftClientRequest;
import models.user.UserInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by topy on 2015/6/13.
 */
public class ThriftFactory {

    private static userservice.Client client = null;

    public synchronized static userservice.Client getClient() {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket("192.168.100.2", 9400));
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new userservice.Client(protocol);

        } catch (TTransportException e) {
        } finally {
            //transport.close();
        }
        return client;
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
        return ThriftConvert.convertK2User(client.login(loginName, password));
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
    public static UserInfo createUser(String nickName, String password, String tel) throws TException {
        return ThriftConvert.convertK2User(client.createUser(nickName, password, tel));
    }

    public static void updateUserInfo(long userId, Map<String, String> userInfo) throws TException {
        client.updateUserInfo(userId, ThriftConvert.convertUserFieldsToPropMap(userInfo));
    }

    public static void modifyPwd(long userId, Map<String, String> userInfo) throws TException {
    }


    /**
     * 添加好友
     *
     * @param userA
     * @param userB
     * @throws TException
     */
    public static void addContact(long userA, long userB) throws TException {
        client.addContact(userA, userB);
    }

    /**
     * 删除好友
     *
     * @param userA
     * @param userB
     * @throws TException
     */
    public static void removeContact(long userA, long userB) throws TException {
        client.removeContact(userA, userB);
    }

    public static List<UserInfo> getContactList(long userId) throws TException {
        List<UserInfoProp> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(UserInfoProp.USER_ID, UserInfoProp.NICK_NAME, UserInfoProp.TEL, UserInfoProp.AVATAR
                , UserInfoProp.GENDER, UserInfoProp.SIGNATURE));
        List<com.aizou.yunkai.UserInfo> contactList = client.getContactList(userId, fields, 0, 1000);
        List<UserInfo> result = new ArrayList<>();

        for (com.aizou.yunkai.UserInfo userInfo : contactList) {
            result.add(ThriftConvert.convertK2User(userInfo));
        }
        return result;
    }

    public static void createChatGroup(long userId, String name, List<Long> participants, Map<String, String> groupFields) throws TException {
        Map<ChatGroupProp, String> userInfoPropStringMap = ThriftConvert.convertGroupFieldsToPropMap(groupFields);
        client.createChatGroup(userId, name, participants, userInfoPropStringMap);
    }

    public static void main(String[] args) throws TException {
        System.out.print("-----");
        //userservice.Client client = ThriftFactory.getClient();
        //System.out.print(client.toString());
        //UserInfo ckdemima = client.login("18514007293", "ckdemima");

        TTransport transport = new TFramedTransport(new TSocket("192.168.100.2", 9400));
//
        TProtocol protocol = new TBinaryProtocol(transport);
        userservice.Client client1 = new userservice.Client(protocol);
        transport.open();
        com.aizou.yunkai.UserInfo userById = client1.getUserById(100000);
        System.out.print("--1---");
        System.out.print(userById.getUserId());
        transport.close();

    }


}
