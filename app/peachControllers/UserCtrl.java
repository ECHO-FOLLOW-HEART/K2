package peachControllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.misc.Token;
import models.morphia.plan.Plan;
import models.morphia.user.UserInfo;
import org.apache.commons.io.IOUtils;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DataConvert.UserConvert;
import utils.LogUtils;
import utils.MsgConstants;
import utils.Utils;
import utils.builder.UserBuilder;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户相关的Controller。
 * <p>
 * Created by topy on 2014/10/10.
 */
public class UserCtrl extends Controller {


    public static int CAPTCHA_ACTION_SIGNUP = 1;
    public static int CAPTCHA_ACTION_MODPWD = 2;
    public static int CAPTCHA_ACTION_BANDTEL = 3;
    public static int CAPTCHA_ACTION_RESET_BANDTEL = 4;

    /**
     * 手机注册
     *
     * @return
     */
    public static Result register() {
        JsonNode req = request().body().asJson();
        try {
            String tel = req.get("tel").asText();
            String pwd = req.get("pwd").asText();
            Integer countryCode;
            String captcha = req.get("captcha").asText();
            if (req.has("countryCode")) {
                countryCode = Integer.valueOf(req.get("countryCode").asText());
            } else {
                countryCode = 86;
            }
            int userId = 0;
            if (req.has("userId"))
                userId = Integer.valueOf(req.get("userId").asText());

            //验证用户是否存在
            if (UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel) != null) {
                return Utils.createResponse(MsgConstants.USER_TEL_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }

            UserInfo userInfo;
            //验证验证码 magic captcha
            if (captcha.equals("85438734") || UserAPI.checkValidation(countryCode, tel, 1, captcha, userId)) {
                // 生成用户
                userInfo = UserAPI.regByTel(tel, countryCode, pwd);
            } else
                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);

            if (userInfo != null)
                return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfo, UserBuilder.DETAILS_LEVEL_1));
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Error");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 验证验证码,返回Token
     *
     * @return
     */
    public static Result checkCaptcha() {
        JsonNode req = request().body().asJson();
        String tel = req.get("tel").asText();
        String captcha = req.get("captcha").asText();
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = Integer.valueOf(req.get("userId").asText());
        int countryCode = 86;
        if (req.has("countryCode"))
            countryCode = Integer.valueOf(req.get("countryCode").asText());

        ObjectNode result = Json.newObject();
        try {
            if (UserAPI.checkValidation(countryCode, tel, actionCode, captcha, userId)) {
                Token token = UserAPI.valCodetoToken(countryCode, tel, actionCode, userId, 600 * 1000);
                result.put("token", token.value);
                result.put("isValid", true);
            } else
                result.put("isValid", false);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 绑定手机
     *
     * @return
     */
    public static Result bindTel() {
        JsonNode req = request().body().asJson();
        UserInfo userInfo;
        String tel = req.get("tel").asText();
        String captcha = req.get("captcha").asText();
        Integer countryCode;
        String pwd = req.has("pwd") ? req.get("pwd").asText() : "";
        String userId = req.get("userId").asText();
        if (req.has("countryCode")) {
            countryCode = Integer.valueOf(req.get("countryCode").asText());
        } else {
            countryCode = 86;
        }
        //验证验证码
        try {
            if (UserAPI.checkValidation(countryCode, tel, CAPTCHA_ACTION_BANDTEL, captcha, Integer.valueOf(userId))) {
                //如果手机已存在，则绑定无效
                if (UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel) != null) {
                    return Utils.createResponse(MsgConstants.USER_EXIST, MsgConstants.USER_EXIST_MSG, true);
                }
                userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.USERID, userId);
                userInfo.tel = tel;
                UserAPI.saveUserInfo(userInfo);
                if (!pwd.equals(""))
                    UserAPI.regCredential(userInfo, pwd);
                return Utils.createResponse(ErrorCode.NORMAL, "Success!");
            } else {
                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @return
     */
    public static Result modPassword() {
        JsonNode req = request().body().asJson();
        String userId = req.get("userId").asText();
        String oldPwd = req.get("oldPwd").asText();
        String newPwd = req.get("newPwd").asText();
        Integer countryCode;
        if (req.has("countryCode")) {
            countryCode = Integer.valueOf(req.get("countryCode").asText());
        } else {
            countryCode = 86;
        }

        //验证用户是否存在-手机号
        try {
            UserInfo userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.USERID, userId);
            if (userInfo == null)
                return Utils.createResponse(MsgConstants.USER_NOT_EXIST, MsgConstants.USER_NOT_EXIST_MSG, true);

            //验证密码
            if (UserAPI.validCredential(userInfo, oldPwd)) {
                //重设密码
                UserAPI.resetPwd(userInfo, newPwd);
                return Utils.createResponse(ErrorCode.NORMAL, "Success!");
            } else
                return Utils.createResponse(MsgConstants.PWD_ERROR, MsgConstants.PWD_ERROR_MSG, true);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 重新设密码
     *
     * @return
     */
    public static Result newPassword() {
        JsonNode req = request().body().asJson();
        String pwd = req.get("pwd").asText();
        String token = req.get("token").asText();
        String userId = req.get("userId").asText();
        Integer countryCode = 86;
        if (req.has("countryCode"))
            countryCode = Integer.valueOf(req.get("countryCode").asText());

        //验证密码格式
        if (!validityPwd(pwd)) {
            return Utils.createResponse(MsgConstants.PWD_FORMAT_ERROR, MsgConstants.PWD_FORMAT_ERROR_MSG, true);
        }
        //验证Token
        try {
            if (UserAPI.checkToken(token, Integer.valueOf(userId), CAPTCHA_ACTION_MODPWD)) {
                UserInfo userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.USERID, userId);
                UserAPI.resetPwd(userInfo, pwd);
                return Utils.createResponse(ErrorCode.NORMAL, "Success!");
            } else
                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
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
    public static Result sendCaptcha() {

        JsonNode req = request().body().asJson();

        String tel = req.get("tel").asText();
        Integer countryCode = req.has("countryCode") ? Integer.valueOf(req.get("countryCode").asText()) : 86;
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = Integer.valueOf(req.get("userId").asText());
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //验证用户是否存在
        try {
            UserInfo us = UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel);
            if (actionCode == CAPTCHA_ACTION_SIGNUP) {
                if (us != null) {
                    return Utils.createResponse(MsgConstants.USER_TEL_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
                }
            } else if (actionCode == CAPTCHA_ACTION_MODPWD) {
                if (us == null) {
                    return Utils.createResponse(MsgConstants.USER_TEL_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true);
                }
            } else if (actionCode == CAPTCHA_ACTION_BANDTEL) {
                if (us != null) {
                    return Utils.createResponse(MsgConstants.USER_TEL_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
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
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 手机号登录
     *
     * @return
     */
    public static Result signin() {

        JsonNode req = request().body().asJson();
        try {
            UserInfo userInfo = null;
            String pwd = req.get("pwd").asText();
            String loginName = req.get("loginName").asText();
            //如果密码为空,视为密码错误
            if (pwd.equals(""))
                return Utils.createResponse(MsgConstants.PWD_ERROR, MsgConstants.PWD_ERROR_MSG, true);
            //验证用户是否存在-手机号
            userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.TEL, loginName);
            //if (userInfo == null)
            //验证用户是否存在-昵称
            //userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, loginName);
            //if (userInfo == null)
            //验证用户是否存在-用户ID
            //userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.USERID, loginName);
            if (userInfo == null)
                return Utils.createResponse(MsgConstants.USER_NOT_EXIST, MsgConstants.USER_NOT_EXIST_MSG, true);

            //验证密码
            if (UserAPI.validCredential(userInfo, pwd))
                return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfo, UserBuilder.DETAILS_LEVEL_1));
            else
                return Utils.createResponse(MsgConstants.PWD_ERROR, MsgConstants.PWD_ERROR_MSG, true);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

    }

    /**
     * 检验用户信息的有效性
     *
     * @return
     */
    public static Result validityInfo(String tel, String nick) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        try {
            if (UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel) != null)
                return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
            if (UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, nick) != null) {
                return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
            }

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.add("valid", true).get()));
    }


    /**
     * 第三方注册
     *
     * @return
     */
    public static Result authRegister() {
        JsonNode req = request().body().asJson();
        String appid, secret, urlAccess, urlDomain, urlInfo, code;
        try {
            code = req.get("code").asText();
            Configuration config = Configuration.root();
            Map wx = (Map) config.getObject("wx");
            appid = wx.get("appid").toString();
            secret = wx.get("secret").toString();
            urlDomain = wx.get("domain").toString();
            urlAccess = wx.get("urlaccess").toString();
            urlInfo = wx.get("urlinfo").toString();

        } catch (NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_CONFARG, "Parameter Not Config");
        }

        try {
            //请求access_token
            String acc_url = getAccessUrl(urlDomain, urlAccess, appid, secret, code);
            URL url = new URL(acc_url);
            String json = IOUtils.toString(url);
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
            json = IOUtils.toString(url);
            m = new ObjectMapper();
            infoNode = m.readTree(json);

            UserInfo us;

            if (!infoNode.has("openid")) {
                return Utils.createResponse(ErrorCode.WEIXIN_CODE_ERROR, MsgConstants.WEIXIN_ACESS_ERROR_MSG, true);
            }

            //如果第三方用户已存在,视为第二次登录
            us = UserAPI.getUserByField(UserAPI.UserInfoField.OPENID, infoNode.get("openid").asText());
            if (us != null) {
                return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(us, UserBuilder.DETAILS_LEVEL_1));
            }

            //JSON转化为userInfo
            us = UserConvert.oauthToUserInfoForWX(infoNode);
            //如果第三方昵称已被其他用户使用，则添加后缀
            if (UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, us.nickName) != null) {
                nickDuplicateRemoval(us);
            }
            UserAPI.saveUserInfo(us);
            // 第三方注册时,注册环信
            UserAPI.regCredential(us, "");
            return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(us, UserBuilder.DETAILS_LEVEL_1));


        } catch (IOException | NullPointerException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 截取userID的后3位，区分重复的昵称
     *
     * @param u
     */
    private static void nickDuplicateRemoval(UserInfo u) {
        String uidStr = u.userId.toString();
        int size = uidStr.length();
        String doc = uidStr.substring(size - 4, size - 1);
        u.nickName = u.nickName + "_" + doc;
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

        StringBuffer info_url = new StringBuffer(10);
        info_url.append("https://");
        info_url.append(urlDomain);
        info_url.append(urlInfo);
        info_url.append("?");
        info_url.append("access_token=");
        info_url.append(access_token);
        info_url.append("&");
        info_url.append("openid=");
        info_url.append(openId);

        return info_url.toString();
    }

    /**
     * 通过id获得用户详细信息。
     *
     * @param userId
     * @return
     */
    public static Result getUserProfileById(String userId) throws UnknownHostException {

        try {

            UserInfo userInfor = UserAPI.getUserByUserId(Integer.valueOf(userId));
            if (userInfor == null)
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "User not exist.");
            return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfor, UserBuilder.DETAILS_LEVEL_1));

        } catch (TravelPiException | ClassCastException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }

    /**
     * 编辑用户资料。
     *
     * @param userId
     * @return
     */
    public static Result editorUserInfo(String userId) throws UnknownHostException {
        try {
            JsonNode req = request().body().asJson();

            UserInfo userInfor = UserAPI.getUserByUserId(Integer.valueOf(userId));
            if (userInfor == null) {
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, String.format("Not exist user id: %s.", userId));
            }
            //修改昵称
            if (req.has("nickName")) {
                String nickName = req.get("nickName").asText();
                // TODO 跟踪乱码问题
                LogUtils.info(Plan.class, "NickName in POST:" + nickName);
                //如果昵称不存在
                if (UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, nickName) == null)
                    userInfor.nickName = nickName;
                else
                    return Utils.createResponse(MsgConstants.NICKNAME_EXIST, MsgConstants.NICKNAME_EXIST_MSG, true);
            }
            //修改签名
            if (req.has("signature"))
                userInfor.signature = req.get("signature").asText();
            //修改性别
            if (req.has("gender")) {
                String genderStr = req.get("gender").asText();
                if ((!genderStr.equals("F")) && (!genderStr.equals("M"))) {
                    Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid gender");
                }
                userInfor.gender = genderStr;
            }
            //修改头像
            if (req.has("avatar"))
                userInfor.avatar = req.get("avatar").asText();
            UserAPI.saveUserInfo(userInfor);
            // TODO 跟踪乱码问题
            LogUtils.info(Plan.class, "NickName in Mongo:" + UserAPI.getUserByUserId(userInfor.userId).nickName);
            LogUtils.info(Plan.class, request());
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (NullPointerException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }
}
