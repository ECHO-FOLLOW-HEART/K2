package controllers.appImpl;

import aizou.core.UserAPI;
import aspectj.Key;
import aspectj.RemoveOcsCache;
import aspectj.UsingOcsCache;
import asynchronous.AsyncExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.thrift.ThriftConvert;
import controllers.thrift.ThriftFactory;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.user.CredentialFormatter;
import formatter.taozi.user.UserFormatterOld;
import formatter.taozi.user.UserInfoFormatter;
import models.user.Credential;
import models.user.UserInfo;
import org.apache.thrift.TException;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MsgConstants;
import utils.Utils;
import utils.phone.PhoneEntity;
import utils.phone.PhoneParserFactory;

import java.util.*;

/**
 * Created by topy on 2015/6/13.
 */
public class UserCtrlImpl_V20 extends Controller {

    public static int CAPTCHA_ACTION_SIGNUP = 1;
    public static int CAPTCHA_ACTION_MODPWD = 2;
    public static int CAPTCHA_ACTION_BANDTEL = 3;
    public static final String FIELD_GUID = "GUID";
    public static long PAIPAI_USERID = 10000;
    public static final String PAIPAI_ESMOB = "xtx2xbxlggo4imqh76kzu5xb86e86yc7";
    public static final String PAIPAI_WELCOME_1 = "你好，我是旅行达人派派，欢迎使用旅行派。";
    public static final String PAIPAI_WELCOME_2 = "你可以用旅行派制做行程计划、收集购物美食。还可以跟旅行达人们交流互动，获取帮助和建议噢。";

    public static Result singinImpl(String loginName, String passwd) throws AizouException, TException {
        PhoneEntity telEntry = null;
        try {
            telEntry = PhoneParserFactory.newInstance().parse(loginName);
        } catch (IllegalArgumentException ignore) {
        }

        ArrayList<Object> valueList = new ArrayList<>();
        valueList.add(loginName);
        if (telEntry != null && telEntry.getPhoneNumber() != null)
            valueList.add(telEntry.getPhoneNumber());

        UserInfo user = ThriftFactory.longin(loginName, passwd);


        UserFormatterOld userFormatter = new UserFormatterOld(true);

        // 服务号发消息
        // TODO 发消息放入消息队列
        UserAPI.sendMessageToUser(PAIPAI_ESMOB, user, PAIPAI_WELCOME_1);
        UserAPI.sendMessageToUser(PAIPAI_ESMOB, user, PAIPAI_WELCOME_2);

        // TODO 验证密码失败怎么处理
        return Utils.createResponse(ErrorCode.NORMAL, userFormatter.format(user));
    }

    public static Result signupImpl(PhoneEntity telEntry, String pwd, String captcha) throws AizouException, TException {

        //验证用户是否存在
        // TODO Yunkai缺少搜索用户
        if (UserAPI.getUserByField(UserInfo.fnTel, telEntry.getPhoneNumber(),
                Arrays.asList(UserInfo.fnUserId)) != null) {
            return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true);
        }

        UserInfo userInfo;
        // TODO Yunkai缺少验证验证码
        if (captcha.equals("85438734") || UserAPI.checkValidation(telEntry.getDialCode(), telEntry.getPhoneNumber()
                , 1, captcha, null)) {
            // 生成用户
            userInfo = ThriftFactory.createUser("旅行派_" + telEntry.getPhoneNumber(), pwd, telEntry.getPhoneNumber());
        } else
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);

        ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(userInfo);

        //添加服务号
        // TODO 发消息放入消息队列
        addContactImpl(userInfo.getUserId(), PAIPAI_USERID);
        return Utils.createResponse(ErrorCode.NORMAL, info);
    }

    @RemoveOcsCache(keyList = "getContactList({userA})|getContactList({userB})")
    public static F.Promise<Result> addContactImpl(@Key(tag = "userA") final long userId,
                                                   @Key(tag = "userB") final long contactId)
            throws AizouException {
        return AsyncExecutor.execute(
                new F.Function0<Object>() {
                    @Override
                    public Object apply() throws Throwable {
                        ThriftFactory.addContact(userId, contactId);
                        return null;
                    }
                },
                new F.Function<Object, Result>() {
                    @Override
                    public Result apply(Object o) throws Throwable {
                        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
                    }
                }
        );
    }

    @RemoveOcsCache(keyList = "getContactList({userA})|getContactList({userB})")
    public static Result delContactImpl(@Key(tag = "userA") long userA, @Key(tag = "userB") long userB)
            throws AizouException, TException {
        ThriftFactory.removeContact(userA, userB);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 获得好友列表
     *
     * @return
     */
    @UsingOcsCache(key = "getContactList({id})", expireTime = 300)
    public static Result getContactListImpl(@Key(tag = "id") long userId) throws AizouException, TException {
        List<UserInfo> list = ThriftFactory.getContactList(userId);
        if (list == null)
            list = new ArrayList<>();

        // TODO Yunkai里应包含备注信息
        // 查询备注信息
        list = UserAPI.addUserMemo(userId, list);
        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
        formatter.setSelfView(false);

        List<JsonNode> nodelist = new ArrayList<>();
        for (UserInfo userInfo : list) {
            nodelist.add(formatter.formatNode(userInfo));
        }

        ObjectNode node = Json.newObject();
        node.put("contacts", Json.toJson(nodelist));
        return Utils.createResponse(ErrorCode.NORMAL, node);
    }


}