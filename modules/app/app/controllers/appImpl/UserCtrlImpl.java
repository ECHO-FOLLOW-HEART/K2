package controllers.appImpl;

import aizou.core.UserAPI;
import aspectj.Key;
import aspectj.RemoveOcsCache;
import aspectj.UsingOcsCache;
import asynchronous.AsyncExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.user.CredentialFormatter;
import formatter.taozi.user.UserFormatterOld;
import formatter.taozi.user.UserInfoFormatter;
import models.user.Credential;
import models.user.UserInfo;
import org.apache.commons.io.IOUtils;
import play.Configuration;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MsgConstants;
import utils.Utils;
import utils.phone.PhoneEntity;
import utils.phone.PhoneParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by topy on 2015/6/13.
 */
public class UserCtrlImpl implements ICtrlImpl {

    public static int CAPTCHA_ACTION_SIGNUP = 1;
    public static int CAPTCHA_ACTION_MODPWD = 2;
    public static int CAPTCHA_ACTION_BANDTEL = 3;
    public static final String FIELD_GUID = "GUID";
    public static long PAIPAI_USERID = 10000;
    public static final String PAIPAI_ESMOB = "xtx2xbxlggo4imqh76kzu5xb86e86yc7";
    public static final String PAIPAI_WELCOME_1 = "你好，我是旅行达人派派，欢迎使用旅行派。";
    public static final String PAIPAI_WELCOME_2 = "你可以用旅行派制做行程计划、收集购物美食。还可以跟旅行达人们交流互动，获取帮助和建议噢。";

    /**
     * 登录 支持手机号登录和昵称登录
     *
     * @param loginName
     * @param passwd
     * @return
     * @throws AizouException
     */
    public static Result singinImpl(String loginName, String passwd) throws AizouException {
        PhoneEntity telEntry = null;
        try {
            telEntry = PhoneParserFactory.newInstance().parse(loginName);
        } catch (IllegalArgumentException ignore) {
        }

        ArrayList<Object> valueList = new ArrayList<>();
        valueList.add(loginName);
        if (telEntry != null && telEntry.getPhoneNumber() != null)
            valueList.add(telEntry.getPhoneNumber());

        UserFormatterOld userFormatter = new UserFormatterOld(true);

        Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnTel, UserInfo.fnNickName), valueList,
                userFormatter.getFilteredFields(), 0, 1);
        if (!itr.hasNext())
            return Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_NOT_EXIST_MSG, true);
        UserInfo userInfo = itr.next();

        //验证密码
        if ((!passwd.equals("")) && UserAPI.validCredential(userInfo, passwd)) {
            ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(userInfo);

            Credential cre = UserAPI.getCredentialByUserId(userInfo.getUserId(),
                    Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));
            if (cre == null)
                throw new AizouException(ErrorCode.USER_NOT_EXIST, "");

            // 机密数据
            JsonNode creNode = new CredentialFormatter().format(cre);
            for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                info.put(entry.getKey(), entry.getValue());
            }

            // 服务号发消息
            UserAPI.sendMessageToUser(PAIPAI_ESMOB, userInfo, PAIPAI_WELCOME_1);
            UserAPI.sendMessageToUser(PAIPAI_ESMOB, userInfo, PAIPAI_WELCOME_2);
            return Utils.createResponse(ErrorCode.NORMAL, info);
        } else
            return Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true);
    }

    public static Result signupImpl(PhoneEntity telEntry, String pwd, String captcha) throws AizouException {

        //验证用户是否存在
        if (UserAPI.getUserByField(UserInfo.fnTel, telEntry.getPhoneNumber(),
                Arrays.asList(UserInfo.fnUserId)) != null) {
            return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true);
        }

        UserInfo userInfo;
        //验证验证码 magic captcha
        if (captcha.equals("85438734") || UserAPI.checkValidation(telEntry.getDialCode(), telEntry.getPhoneNumber()
                , 1, captcha, null)) {
            // 生成用户
            userInfo = UserAPI.regByTel(telEntry.getPhoneNumber(), telEntry.getDialCode(), pwd);
        } else
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);

        ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(userInfo);

        Credential cre = UserAPI.getCredentialByUserId(userInfo.getUserId(),
                Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));

        // 机密数据
        JsonNode creNode = new CredentialFormatter().format(cre);
        for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            info.put(entry.getKey(), entry.getValue());
        }

        //添加服务号
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
                        UserAPI.addContact(userId, contactId);
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
            throws AizouException {
        UserAPI.delContact(userA, userB);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 获得好友列表
     *
     * @return
     */
    @UsingOcsCache(key = "getContactList({id})", expireTime = 300)
    public static Result getContactListImpl(@Key(tag = "id") long userId) throws AizouException {
        List<UserInfo> list = UserAPI.getContactList(userId);
        if (list == null)
            list = new ArrayList<>();

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

    public static Result authRegisterImpl(String code) throws AizouException{
        String appid, secret, urlAccess, urlDomain, urlInfo;

        Configuration config = Configuration.root();
        Map wx = (Map) config.getObject("wx");
        appid = wx.get("appid").toString();
        secret = wx.get("secret").toString();
        urlDomain = wx.get("domain").toString();
        urlAccess = wx.get("urlaccess").toString();
        urlInfo = wx.get("urlinfo").toString();

        //请求access_token
        String acc_url = getAccessUrl(urlDomain, urlAccess, appid, secret, code);
        try {

            URL url = new URL(acc_url);
            String json = IOUtils.toString(url, "UTF-8");
            ObjectMapper m = new ObjectMapper();
            JsonNode rootNode = m.readTree(json);
            String access_token, openId, info_url;
            JsonNode infoNode;

            //如果请求失败
            if (rootNode.has("errcode"))
                return Utils.createResponse(ErrorCode.WEIXIN_CODE_ERROR, MsgConstants.WEIXIN_ACESS_ERROR_MSG, true);
            //获取access_token
            access_token = rootNode.get("access_token").asText();
            openId = rootNode.get("openid").asText();

            //请求用户信息
            info_url = getInfoUrl(urlDomain, urlInfo, access_token, openId);
            url = new URL(info_url);
            json = IOUtils.toString(url, "UTF-8");
            m = new ObjectMapper();
            infoNode = m.readTree(json);

            UserInfo us;

            if (!infoNode.has("openid")) {
                return Utils.createResponse(ErrorCode.WEIXIN_CODE_ERROR, MsgConstants.WEIXIN_ACESS_ERROR_MSG, true);
            }

            //如果第三方用户已存在,视为第二次登录
            us = UserAPI.getUserByField(UserInfo.fnOauthId, infoNode.get("openid").asText());
            if (us != null) {
                ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(us);

                Credential cre = UserAPI.getCredentialByUserId(us.getUserId(),
                        Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));
                if (cre == null)
                    throw new AizouException(ErrorCode.USER_NOT_EXIST, "");

                // 机密数据
                JsonNode creNode = new CredentialFormatter().format(cre);
                for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    info.put(entry.getKey(), entry.getValue());
                }

                // 服务号发消息
                UserAPI.sendMessageToUser(PAIPAI_ESMOB, us, PAIPAI_WELCOME_1);
                UserAPI.sendMessageToUser(PAIPAI_ESMOB, us, PAIPAI_WELCOME_2);
                return Utils.createResponse(ErrorCode.NORMAL, info);
            }

            //JSON转化为userInfo
            us = UserAPI.oauthToUserInfoForWX(infoNode);
            if (UserAPI.getUserByField(UserInfo.fnNickName, us.getNickName()) != null) {
                //如果第三方昵称为纯数字，则添加后缀
                if (Utils.isNumeric(us.getNickName())) {
                    us.setNickName(us.getNickName() + "_桃子");
                    us.setAlias(Arrays.asList(us.getNickName().toLowerCase()));
                }
                //如果第三方昵称已被其他用户使用，则添加后缀
                nickDuplicateRemoval(us);
            }

            //注册信息
            UserInfo userInfo = UserAPI.regByWeiXin(us);

            //返回注册信息
            ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(userInfo);

            Credential cre = UserAPI.getCredentialByUserId(userInfo.getUserId(),
                    Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));
            if (cre == null)
                throw new AizouException(ErrorCode.USER_NOT_EXIST, "Credential info is null.");

            // 返回机密数据
            JsonNode creNode = new CredentialFormatter().format(cre);
            for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                info.put(entry.getKey(), entry.getValue());
            }

            //添加服务号
            UserCtrlImpl.addContactImpl(userInfo.getUserId(), PAIPAI_USERID);
            // 服务号发消息
            UserAPI.sendMessageToUser(PAIPAI_ESMOB, userInfo, PAIPAI_WELCOME_1);
            UserAPI.sendMessageToUser(PAIPAI_ESMOB, userInfo, PAIPAI_WELCOME_2);
            return Utils.createResponse(ErrorCode.NORMAL, info);
        } catch (IOException e) {
            throw new AizouException(ErrorCode.IO_ERROR, "", e);
        }
    }

    /**
     * 截取userID的后3位，区分重复的昵称
     *
     * @param u
     */
    private static void nickDuplicateRemoval(UserInfo u) {
        String uidStr = u.getUserId().toString();
        int size = uidStr.length();
        String doc = uidStr.substring(size - 4, size - 1);
        u.setNickName(u.getNickName() + "_" + doc);
        u.setAlias(Arrays.asList(u.getNickName().toLowerCase()));
    }

    private static String getAccessUrl(String urlDomain, String urlAccess, String appid, String secret, String code) {
        StringBuffer acc_url = new StringBuffer(10);
        acc_url.append("https://");
        acc_url.append(urlDomain);
        acc_url.append(urlAccess);
        acc_url.append("?");
        acc_url.append("appid=");
        acc_url.append(appid);
        acc_url.append("&");
        acc_url.append("secret=");
        acc_url.append(secret);
        acc_url.append("&");
        acc_url.append("code=");
        acc_url.append(code);
        acc_url.append("&");
        acc_url.append("grant_type=");
        acc_url.append("authorization_code");
        return acc_url.toString();
    }

    private static String getInfoUrl(String urlDomain, String urlInfo, String access_token, String openId) {

        return "https://" + urlDomain + urlInfo + "?" + "access_token=" + access_token + "&" + "openid=" + openId;
    }

}
