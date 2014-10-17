package peachControllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObjectBuilder;
import core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.user.UserInfo;
import org.apache.commons.io.IOUtils;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DataConvert.UserConvert;
import utils.MsgConstants;
import utils.Utils;
import utils.builder.UserBuilder;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * 用户相关的Controller。
 * <p>
 * Created by topy on 2014/10/10.
 */
public class UserCtrl extends Controller {


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

            //验证用户是否存在
            if (UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel) != null) {
                return Utils.createResponse(MsgConstants.USER_EXIST, MsgConstants.USER_EXIST_MSG);
            }

            UserInfo userInfo;
            //验证验证码
            if (UserAPI.checkValidation(countryCode, tel, 1, captcha)) {
                // 生成用户
                userInfo = UserAPI.regByTel(tel, countryCode);
                UserAPI.regCredential(userInfo, pwd);

            } else {
                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG);
            }

            if (userInfo != null)
                return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfo, UserBuilder.DETAILS_LEVEL_1));
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Error");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 绑定手机
     *
     * @return
     */
    public static Result bandTel() {
        JsonNode req = request().body().asJson();
        String tel = req.get("tel").asText();
        String captcha = req.get("captcha").asText();
        Integer countryCode;
        if (req.has("countryCode")) {
            countryCode = Integer.valueOf(req.get("countryCode").asText());
        } else {
            countryCode = 86;
        }
        //验证验证码
        try {
            if (UserAPI.checkValidation(countryCode, tel, 1, captcha))
                return null;
            else {
                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG);
            }
        } catch (TravelPiException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * 找回密码
//     *
//     * @return
//     */
//    public static Result findPassword.() {
//        JsonNode req = request().body().asJson();
//        String tel = req.get("tel").asText();
//        String captcha = req.get("captcha").asText();
//        Integer countryCode;
//        if (req.has("countryCode")) {
//            countryCode = Integer.valueOf(req.get("countryCode").asText());
//        } else {
//            countryCode = 86;
//        }
//        //验证验证码
//        try {
//            if (UserAPI.checkValidation(countryCode, tel, 1, captcha))
//                return null;
//            else {
//                return Utils.createResponse(MsgConstants.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG);
//            }
//        } catch (TravelPiException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
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
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //验证用户是否存在
        try {
            if (UserAPI.getUserByField(UserAPI.UserInfoField.TEL, tel) != null) {
                return Utils.createResponse(MsgConstants.USER_EXIST, MsgConstants.USER_EXIST_MSG);
            }
            Configuration config = Configuration.root();
            Map sms = (Map) config.getObject("sms");
            long expireMs = Long.valueOf(sms.get("signupExpire").toString());
            long resendMs = Long.valueOf(sms.get("resendInterval").toString());
            //注册发验证码-1，找回密码-2，绑定手机-3

            //注册发送短信
            UserAPI.sendValCode(countryCode, tel, actionCode, expireMs * 1000, resendMs * 1000);
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
                //验证用户是否存在-手机号
                userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.TEL, loginName);
            if (userInfo == null)
                //验证用户是否存在-昵称
                userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, loginName);
            if (userInfo == null)
                //验证用户是否存在-用户ID
                userInfo = UserAPI.getUserByField(UserAPI.UserInfoField.USERID, loginName);
            if (userInfo == null)
                return Utils.createResponse(MsgConstants.USER_NOT_EXIST, MsgConstants.USER_NOT_EXIST_MSG);

            //验证密码
            if (UserAPI.validCredential(userInfo, pwd))
                return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfo, UserBuilder.DETAILS_LEVEL_1));
            else
                return Utils.createResponse(MsgConstants.PWD_ERROR, MsgConstants.PWD_ERROR_MSG);
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
                return Utils.createResponse(ErrorCode.WEIXIN_CODE_ERROR, "Wei Xin invalid code ");
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
                return Utils.createResponse(ErrorCode.WEIXIN_CODE_ERROR, "Wei Xin invalid access token ");
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
                //如果昵称不存在
                if (UserAPI.getUserByField(UserAPI.UserInfoField.NICKNAME, nickName) == null)
                    userInfor.nickName = nickName;
                else
                    return Utils.createResponse(MsgConstants.NICKNAME_EXIST, MsgConstants.NICKNAME_EXIST_MSG);
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
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (NullPointerException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }
}
