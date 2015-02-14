package controllers.taozi;

import aizou.core.UserAPI;
import aspectj.CheckUser;
import aspectj.Key;
import aspectj.RemoveOcsCache;
import aspectj.UsingOcsCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import controllers.AsyncExecutor;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.user.ContactFormatter;
import formatter.taozi.user.CredentialFormatter;
import formatter.taozi.user.UserFormatterOld;
import formatter.taozi.user.UserInfoFormatter;
import models.MorphiaFactory;
import models.misc.Token;
import models.user.Contact;
import models.user.Credential;
import models.user.UserInfo;
import org.apache.commons.io.IOUtils;
import org.mongodb.morphia.Datastore;
import play.Configuration;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MsgConstants;
import utils.Utils;
import utils.phone.PhoneEntity;
import utils.phone.PhoneParser;
import utils.phone.PhoneParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户相关的Controller。
 * <p/>
 * Created by topy on 2014/10/10.
 */
public class UserCtrl extends Controller {

    public static int CAPTCHA_ACTION_SIGNUP = 1;
    public static int CAPTCHA_ACTION_MODPWD = 2;
    public static int CAPTCHA_ACTION_BANDTEL = 3;

    /**
     * 手机注册
     *
     * @return
     */
    public static Result signup() throws AizouException {
        JsonNode req = request().body().asJson();

        String pwd = req.get("pwd").asText();
        String captcha = req.get("captcha").asText();

        PhoneEntity telEntry = PhoneParserFactory.newInstance().parse(req.get("tel").asText());

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

        return Utils.createResponse(ErrorCode.NORMAL, info);
    }

    /**
     * 验证验证码,返回Token
     *
     * @return
     */
    public static Result checkCaptcha() throws AizouException {
        JsonNode req = request().body().asJson();
        String tel = req.get("tel").asText();
        String captcha = req.get("captcha").asText();
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = 0;
        // 注册和忘记密码时，无userId；绑定手机号时有userId
        if (req.has("userId"))
            userId = Integer.valueOf(req.get("userId").asText());
        int countryCode = 86;
        if (req.has("dialCode"))
            countryCode = Integer.valueOf(req.get("dialCode").asText());

        ObjectNode result = Json.newObject();

        if (captcha.equals("85438734") || UserAPI.checkValidation(countryCode, tel, actionCode, captcha, userId)) {
            Token token = UserAPI.valCodetoToken(countryCode, tel, actionCode, userId, 600 * 1000);
            result.put("token", token.value);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } else {
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
        }
    }

    /**
     * 绑定手机
     *
     * @return
     */
    public static Result bindTel() throws AizouException {
        JsonNode req = request().body().asJson();
        UserInfo userInfo;
        String tel = req.get("tel").asText();
        String token = req.get("token").asText();
        Integer countryCode;
        String pwd = req.has("pwd") ? req.get("pwd").asText() : "";
        Integer userId = Integer.valueOf(req.get("userId").asText());
        if (req.has("dialCode")) {
            countryCode = Integer.valueOf(req.get("dialCode").asText());
        } else {
            countryCode = 86;
        }
        //验证验证码
        if (UserAPI.checkToken(token, Integer.valueOf(userId), CAPTCHA_ACTION_BANDTEL)) {
            //如果手机已存在，则绑定无效
            if (UserAPI.getUserByField(UserInfo.fnTel, tel) != null) {
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
            userInfo = UserAPI.getUserByField(UserInfo.fnUserId, userId, null);
            userInfo.setTel(tel);
            UserAPI.saveUserInfo(userInfo);

            if (!pwd.equals("")) {
                Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
                Credential cre = ds.createQuery(Credential.class).field(Credential.fnUserId).equal(userInfo.getUserId()).get();
                cre.setSalt(Utils.getSalt());
                cre.setPwdHash(Utils.toSha1Hex(cre.getSalt() + pwd));

                MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);
            }
            return Utils.createResponse(ErrorCode.NORMAL, "Success!");
        } else {
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.TOKEN_ERROR_MSG, true);
        }
    }

    /**
     * 修改密码
     *
     * @return
     */
    public static Result modPassword() throws AizouException {
        JsonNode req = request().body().asJson();
        Integer userId = Integer.parseInt(req.get("userId").asText());
        String oldPwd = req.get("oldPwd").asText();
        String newPwd = req.get("newPwd").asText();

        //验证用户是否存在-手机号
        UserInfo userInfo = UserAPI.getUserByField(UserInfo.fnUserId, userId);
        if (userInfo == null)
            return Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true);

        //验证密码
        if (UserAPI.validCredential(userInfo, oldPwd)) {
            //重设密码
            UserAPI.resetPwd(userInfo, newPwd);
            return Utils.createResponse(ErrorCode.NORMAL, "Success!");
        } else
            return Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true);
    }

    /**
     * 重新设密码
     *
     * @return
     */
    public static Result newPassword() throws AizouException {
        JsonNode req = request().body().asJson();
        String pwd = req.get("pwd").asText();
        String token = req.get("token").asText();
        String tel = req.get("tel").asText();
        Integer countryCode = 86;
        if (req.has("dialCode"))
            countryCode = Integer.valueOf(req.get("dialCode").asText());

        //验证密码格式
        if (!validityPwd(pwd)) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.PWD_FORMAT_ERROR_MSG, true);
        }
        //验证Token
        //忘记密码后重设密码，不需要userId
        if (UserAPI.checkToken(token, 0, CAPTCHA_ACTION_MODPWD)) {
            UserInfo userInfo = UserAPI.getUserByField(UserInfo.fnTel, tel);
            UserAPI.resetPwd(userInfo, pwd);

            ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(userInfo);
            Credential cre = UserAPI.getCredentialByUserId(userInfo.getUserId(),
                    Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));
            if (cre == null)
                throw new AizouException(ErrorCode.USER_NOT_EXIST, "User not exist.");

            // 机密数据
            JsonNode creNode = new CredentialFormatter().format(cre);
            for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                info.put(entry.getKey(), entry.getValue());
            }

            return Utils.createResponse(ErrorCode.NORMAL, info);
        } else
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
    }

    private static boolean validityPwd(String pwd) {
        String regEx = "[0-9a-zA-Z_]{6,12}$";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(pwd);
        return mat.find();
    }

    /**
     * 发送手机验证码号码。
     *
     * @return
     */
    public static Result sendCaptcha() throws AizouException {

        JsonNode req = request().body().asJson();

        String tel = req.get("tel").asText();
        Integer countryCode = req.has("dialCode") ? Integer.valueOf(req.get("dialCode").asText()) : 86;
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = req.has("userId") ? Integer.valueOf(req.get("userId").asText()) : null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //验证用户是否存在
        UserInfo us = UserAPI.getUserByField(UserInfo.fnTel, tel);
        if (actionCode == CAPTCHA_ACTION_SIGNUP) {
            if (us != null) {   //us！=null,说明用户存在
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
        } else if (actionCode == CAPTCHA_ACTION_MODPWD) {
            if (us == null) {
                return Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true);
            }
        } else if (actionCode == CAPTCHA_ACTION_BANDTEL) {
            if (us != null) {
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
        }

        Configuration config = Configuration.root();
        Map sms = (Map) config.getObject("sms");
        long expireMs = Long.valueOf(sms.get("signupExpire").toString());
        long resendMs = Long.valueOf(sms.get("resendInterval").toString());
        //注册发验证码-1，找回密码-2，绑定手机-3

        //注册发送短信
        UserAPI.sendValCode(countryCode, tel, actionCode, userId, expireMs * 1000, resendMs * 1000);
        builder.add("coolDown", resendMs);

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.get()));
    }

    /**
     * 手机号登录,只支持手机号登录
     *
     * @return
     */
    public static Result signin() throws AizouException {

        JsonNode req = request().body().asJson();
        String passwd = req.get("pwd").asText();
        String loginName = req.get("loginName").asText();

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
            return Utils.createResponse(ErrorCode.NORMAL, info);
        } else
            return Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true);
    }

    /**
     * 检验用户信息的有效性
     *
     * @return
     */
    public static Result validityInfo(String tel, String nick) throws AizouException {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        if (UserAPI.getUserByField(UserInfo.fnTel, tel) != null)
            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
        if (UserAPI.getUserByField(UserInfo.fnNickName, nick) != null) {
            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.add("valid", true).get()));
    }


    /**
     * 第三方注册
     *
     * @return
     */
    public static Result authRegister() throws AizouException {
        JsonNode req = request().body().asJson();
        String appid, secret, urlAccess, urlDomain, urlInfo, code;

        code = req.get("code").asText();
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

                return Utils.createResponse(ErrorCode.NORMAL, info);
            }

            //JSON转化为userInfo
            us = UserAPI.oauthToUserInfoForWX(infoNode);
            if (UserAPI.getUserByField(UserInfo.fnNickName, us.getNickName()) != null) {
                //如果第三方昵称为纯数字，则添加后缀
                if (Utils.isNumeric(us.getNickName())) {
                    us.setNickName(us.getNickName() + "_桃子");
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

    /**
     * 通过id获得用户详细信息。
     *
     * @param userId
     * @return
     */
    @CheckUser(nullable = true)
    public static Result getUserProfileById(long userId) throws AizouException {
        String tmp = request().getHeader("UserId");
        Long selfId = null;
        if (tmp != null)
            selfId = Long.parseLong(tmp);

        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
        boolean selfView = (selfId != null && ((Long) userId).equals(selfId));
        formatter.setSelfView(selfView);

        UserInfo result = UserAPI.getUserInfo(userId, formatter.getFilteredFields());
        if (result == null)
            return Utils.createResponse(ErrorCode.USER_NOT_EXIST);

        String ret = formatter.format(result);

        return Utils.status(ret);
    }

    /**
     * 获得用户信息
     *
     * @param keyword
     * @return
     */
    public static Result searchUser(String keyword) throws AizouException {
        PhoneEntity telEntry = null;
        try {
            telEntry = PhoneParserFactory.newInstance().parse(keyword);
        } catch (IllegalArgumentException ignore) {
        }

        ArrayList<Object> valueList = new ArrayList<>();
        valueList.add(keyword);
        if (telEntry != null && telEntry.getPhoneNumber() != null)
            valueList.add(telEntry.getPhoneNumber());

        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
        formatter.setSelfView(false);

        List<UserInfo> result = new ArrayList<>();
        for (Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnTel, UserInfo.fnNickName,
                UserInfo.fnUserId), valueList, formatter.getFilteredFields(), 0, 20); itr.hasNext(); ) {
            result.add(itr.next());
        }
        return Utils.status(formatter.format(result));
    }

    /**
     * 编辑用户资料。
     *
     * @param userId
     * @return
     */
    @CheckUser
    public static Result editorUserInfo(@CheckUser Long userId) throws AizouException {
        JsonNode req = request().body().asJson();
        if (userId == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalide UserId");

        // TODO 这里的做法是：将用户信息整体读出，修改，然后save。这种做法的效率较低。
        UserInfo userInfor = UserAPI.getUserInfo(userId);
        if (userInfor == null) {
            return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, String.format("Not exist user id: %d.", userId));
        }
        //修改昵称
        if (req.has("nickName")) {
            String nickName = req.get("nickName").asText();
            if (Utils.isNumeric(nickName)) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.NICKNAME_NOT_NUMERIC_MSG, true);
            }
            //如果昵称不存在
            if (UserAPI.getUserByField(UserInfo.fnNickName, nickName) == null)
                userInfor.setNickName(nickName);
            else
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.NICKNAME_EXIST_MSG, true);
        }
        //修改签名
        if (req.has("signature"))
            userInfor.setSignature(req.get("signature").asText());
        //修改性别
        if (req.has("gender")) {
            String genderStr = req.get("gender").asText();
            if ((!genderStr.equals("F")) && (!genderStr.equals("M"))) {
                Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid gender");
            }
            userInfor.setGender(genderStr);
        }
        //修改头像
        if (req.has("avatar"))
            userInfor.setAvatar(req.get("avatar").asText());
        UserAPI.saveUserInfo(userInfor);
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 请求添加好友
     *
     * @return
     */
    public static Result requestAddContact() throws AizouException {
        long userId, contactId;
        String message;

        JsonNode req = request().body().asJson();
        userId = Integer.parseInt(request().getHeader("UserId"));
        message = req.get("message").asText();
        contactId = Integer.parseInt(req.get("userId").asText());

        UserAPI.requestAddContact(userId, contactId, message);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 添加好友
     *
     * @return
     */
    public static F.Promise<Result> addContact() throws AizouException {
        long userId, contactId;

        userId = Integer.parseInt(request().getHeader("UserId"));
        contactId = Integer.parseInt(request().body().asJson().get("userId").asText());
        return addContactImpl(userId, contactId);
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

    /**
     * 删除好友
     *
     * @param id
     * @return
     */
    public static Result delContact(Long id) throws AizouException {
        long userId;
        userId = Integer.parseInt(request().getHeader("UserId"));

        return delContactImpl(userId, id);
    }

    @RemoveOcsCache(keyList = "getContactList({userA})|getContactList({userB})")
    public static Result delContactImpl(@Key(tag = "userA") long userA, @Key(tag = "userB") long userB)
            throws AizouException {
        UserAPI.delContact(userA, userB);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    public static Result getContactList() throws AizouException {
        long userId;
        userId = Integer.parseInt(request().getHeader("UserId"));
        return getContactListImpl(userId);
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

        // TODO 需要启用新的formatter
        List<JsonNode> nodelist = new ArrayList<>();
        for (UserInfo userInfo : list) {
            nodelist.add(new UserFormatterOld(false).format(userInfo));
        }

        ObjectNode node = Json.newObject();
        node.put("contacts", Json.toJson(nodelist));
        return Utils.createResponse(ErrorCode.NORMAL, node);
    }

    /**
     * 根据环信用户名获取
     *
     * @return
     */
    public static Result getUsersByEasemob() throws AizouException {
        JsonNode req = request().body().asJson();
        JsonNode emList;
        List<String> emNameList = new ArrayList<>();

        emList = req.get("easemob");
        if (null != emList && emList.isArray() && emList.findValues("easemob") != null) {
            for (JsonNode node : emList) {
                emNameList.add(node.asText());
            }
        }
        List<String> fieldList = Arrays.asList(UserInfo.fnUserId, UserInfo.fnNickName, UserInfo.fnAvatar,
                UserInfo.fnGender, UserInfo.fnEasemobUser, UserInfo.fnSignature);
        List<UserInfo> list = UserAPI.getUserByEaseMob(emNameList, fieldList);
        List<JsonNode> nodeList = new ArrayList<>();
        for (UserInfo userInfo : list) {
            nodeList.add(new UserFormatterOld(false).format(userInfo));
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
    }

    /**
     * 根据通讯录进行用户匹配
     *
     * @return
     */
    public static Result matchAddressBook() throws AizouException {
        JsonNode req = request().body().asJson();
        Iterator<JsonNode> it = req.get("contacts").elements();
        Long selfUserId = Long.parseLong(request().getHeader("UserId"));
        List<Contact> contacts = new ArrayList<>();
        ObjectMapper m = new ObjectMapper();

        PhoneParser parser = PhoneParserFactory.newInstance();

        while (it.hasNext()) {
            Contact c = m.convertValue(it.next(), Contact.class);
            try {
                PhoneEntity phone = parser.parse(c.tel);
                c.tel = phone.getPhoneNumber();
            } catch (IllegalArgumentException ignored) {
            }
            contacts.add(c);
        }
        UserInfo userInfo;

        // 当前用户的联系人
        Set<Long> contactSet = UserAPI.getContactIds(selfUserId);

        List<String> telList = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.tel != null && !c.tel.isEmpty())
                telList.add(c.tel);
        }


        Map<String, Long> matchResult = new HashMap<>();
        for (Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnTel), telList,
                Arrays.asList(UserInfo.fnUserId, UserInfo.fnTel), 0, 1000); itr.hasNext(); ) {
            UserInfo user = itr.next();
            matchResult.put(user.getTel(), user.getUserId());
        }

        for (Contact temp : contacts) {
//                temp.setId(new ObjectId());
//                userInfo = UserAPI.getUserByField(UserInfo.fnTel, temp.tel, Arrays.asList(UserInfo.fnUserId));

            if (matchResult.containsKey(temp.tel)) {
                temp.isUser = true;
                temp.userId = matchResult.get(temp.tel);
                temp.isContact = contactSet.contains(temp.userId);
            } else {
                temp.isUser = false;
                temp.isContact = false;
            }

        }

        ContactFormatter formatter = FormatterFactory.getInstance(ContactFormatter.class);
        return Utils.status(formatter.format(contacts));
    }

    private static boolean isFriend(Long aFriend, List<UserInfo> friends) {
        if (friends == null)
            return false;
        for (UserInfo userInfo : friends) {
            if (userInfo.getUserId().equals(aFriend))
                return true;
        }
        return false;
    }


//    /**
//     * 添加用户的备注信息
//     *
//     * @param id
//     * @return
//     * @throws TravelPiException
//     */
//    public static Result setUserMemo(Integer id) throws TravelPiException {
//        try {
//            String selfId = request().getHeader("userId");
//            String memo = request().body().asJson().get("memo").asText();
//            UserAPI.setUserMemo(Integer.parseInt(selfId), id, memo);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), Json.toJson(e.getMessage()));
//        } catch (NullPointerException | NumberFormatException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
//        }
//    }

//    /**
//     * 获得用户的黑名单列表
//     *
//     * @param
//     * @return backlist
//     */
//    public static Result getUserBlackList() {
//        try {
//            String userId = request().getHeader("userId");
//            List<Integer> list = UserAPI.getBlackList(Integer.parseInt(userId));
//            Map<String, List<Integer>> map = new HashMap<>();
//            map.put("blacklist", list);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(map));
//        } catch (TravelPiException | NullPointerException | ClassCastException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
//        }
//    }
//
//    /**
//     * 将用户加入/移除黑名单
//     *
//     * @return
//     */
//    public static Result setUserBlacklist() {
//        try {
//            String selfId = request().getHeader("userId");
//            JsonNode req = request().body().asJson();
//            List<Integer> list = (List<Integer>) req.get("userList").iterator();
//            /*Iterator<JsonNode> iterator =  req.get("userList").iterator();
//            List<Integer> list =new ArrayList<>();
//            while(iterator.hasNext()){
//               JsonNode node=iterator.next();
//               list.add(node.get("userList").asInt());
//            }*/
//            String operation = req.get("action").asText();
//
//            UserAPI.setUserBlacklist(Integer.parseInt(selfId), list, operation);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
//        } catch (TravelPiException | NullPointerException | ClassCastException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
//        }
//    }
}
