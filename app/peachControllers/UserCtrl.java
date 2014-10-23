package peachControllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import core.UserAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.user.Credential;
import models.morphia.plan.Plan;
import models.morphia.user.UserInfo;
import play.mvc.Controller;
import play.mvc.Result;
import utils.LogUtils;
import utils.Utils;
import utils.builder.UserBuilder;

import java.net.UnknownHostException;

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

            //验证用户是否存在
            if (UserAPI.getUserByTel(tel) != null) {
                return Utils.createResponse(ErrorCode.DATA_EXIST, "User has exist.");
            }

            //验证验证码
            // TODO
            if (validateCaptcha("1234")) {

                // 生成用户
                UserInfo userInfo = UserAPI.regByTel(tel);
                UserAPI.regCredential(userInfo, pwd);

            } else {
                return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, "Captcha is wrong.");
            }

            UserInfo userInfo = new UserInfo();


            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 验证验证码
     *
     * @param ca
     * @return
     */
    // TODO
    private static boolean validateCaptcha(String ca) {
        return true;
    }

    /**
     * 发送手机验证码号码。
     *
     * @return
     */
    public static Result sendCaptcha() throws UnknownHostException, TravelPiException {

        JsonNode req = request().body().asJson();

        String tel = req.get("tel").asText();
        String pwd = req.get("pwd").asText();

        // TODO 调用SP
        String captcha = "1234";

        // TODO 存入缓存
        Credential ce = new Credential();
        ce.pwdHash = "";
        ce.salt = "";


        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 检验用户信息的有效性
     *
     * @return
     */
    public static Result validityInfo(String tel, String nick) {

        try {
            if (UserAPI.getUserByTel(tel) != null)
                return Utils.createResponse(ErrorCode.DATA_EXIST, "Telephone number has exist:" + tel);
            if (UserAPI.getUserByNickName(nick) != null) {
                return Utils.createResponse(ErrorCode.DATA_EXIST, "Nickname has exist:" + nick);
            }
        } catch (TravelPiException e) {
            e.printStackTrace();
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 验证手机号码是否存在。
     *
     * @param tel
     * @return
     */
    public static Result getUserByTel(String tel) throws UnknownHostException, TravelPiException {
        try {
            UserInfo userInfo = UserAPI.getUserByTel(tel);
            if (userInfo != null) {
                return Utils.createResponse(ErrorCode.DATA_EXIST, "User has exist.");
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 验证昵称是否存在。
     *
     * @param tel
     * @return
     */
    public static Result getUserByNickName(String tel) throws UnknownHostException, TravelPiException {
        try {
            UserInfo userInfo = UserAPI.getUserByTel(tel);
            if (userInfo != null) {
                return Utils.createResponse(ErrorCode.DATA_EXIST, "User has exist.");
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 第三方注册
     *
     * @return
     */
    public static Result authRegister() {
        JsonNode req = request().body().asJson();
        String provider = req.get("provider").asText();
        String oauthId = req.get("oauthId").asText();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"nickName", "avatar", "token", "udid"}) {
            try {
                builder.add(k, req.get(k).asText());
            } catch (NullPointerException ignored) {
            }
        }

        try {
            UserInfo user = UserAPI.regByOAuth(provider, oauthId, builder.get(), "");
            return Utils.createResponse(ErrorCode.NORMAL, user.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 通过id获得用户详细信息。
     *
     * @param userId
     * @return
     */
    public static Result getUserProfileById(String userId) throws UnknownHostException {
        try {

            UserInfo userInfor = UserAPI.getUserByUserId(userId);
            return Utils.createResponse(ErrorCode.NORMAL, UserBuilder.buildUserInfo(userInfor, UserBuilder.DETAILS_LEVEL_2));

        } catch (TravelPiException e) {
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

            String uid = req.get("userId").asText();
            String tel = req.get("tel").asText();
            UserInfo userInfor = UserAPI.getUserByUserId(uid);
            if (userInfor == null) {
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, String.format("Not exist user id: %s.", userId));
            }
            //修改昵称
            if (req.has("nickName")) {
                String nickName = req.get("nickName").asText();
                // TODO 跟踪乱码问题
                LogUtils.info(Plan.class, "NickName in POST:" + nickName);
                //如果昵称不存在
                if (UserAPI.getUserByNickName(nickName) == null) {
                    userInfor.nickName = nickName;
                }
            }
            //修改签名
            if (req.has("signature"))
                userInfor.signature = req.get("signature").asText();
            //修改性别
            if (req.has("gender"))
                userInfor.gender = req.get("gender").asText();
            UserAPI.saveUserInfo(userInfor);
            // TODO 跟踪乱码问题
            LogUtils.info(Plan.class, "NickName in Mongo:" + UserAPI.getUserByUserId(userInfor.userId).nickName);
            LogUtils.info(Plan.class,request());
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (NullPointerException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user id: %s.", userId));
        }
    }
}
