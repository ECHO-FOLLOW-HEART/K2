package utils;

/**
 * Created by topy on 2014/10/16.
 */
public class MsgConstants {

    /**
     * 用户已存在
     */
    public static int USER_EXIST = 405;

    /**
     * 用户已存在
     */
    public static String USER_EXIST_MSG = "该用户已存在.";

    /**
     * 用户不已存在
     */
    public static int USER_NOT_EXIST = 406;

    /**
     * 用户不已存在
     */
    public static String USER_NOT_EXIST_MSG = "该用户不存在.";

    /**
     * 注册时验证码错误
     */
    public static int CAPTCHA_ERROR = 403;

    /**
     * 注册时验证码错误
     */
    public static String CAPTCHA_ERROR_MSG = "验证码不正确，请重新发送";

    /**
     * 注册时验证码错误
     */
    public static String PWD_ERROR_MSG = "您输入的密码不正确";
    /**
     * 昵称已存在
     */
    public static int NICKNAME_EXIST = 408;

    /**
     * 昵称已存在
     */
    public static String NICKNAME_EXIST_MSG = "您输入的昵称已经被注册";

    /**
     * 新设密码格式不正确
     */
    public static int PWD_FORMAT_ERROR = 409;

    /**
     * 新设密码格式不正确
     */
    public static String PWD_FORMAT_ERROR_MSG = "请设置6-10位字母或数字";

    /**
     * 用户已存在
     */
    public static int USER_TEL_EXIST = 411;

    /**
     * 用户已存在
     */
    public static String USER_TEL_EXIST_MSG = "您输入的手机号码已经被注册";

    /**
     * 用户已存在
     */
    public static int USER_TEL_NOT_EXIST = 412;

    /**
     * 用户已存在
     */
    public static String USER_TEL_NOT_EXIST_MSG = "您输入的手机号码尚未注册";

    /**
     * 微信注册时请求access_token失败
     */
    public static int WEIXIN_ACESS_ERROR = 404;

    /**
     * 微信注册时请求用户信息失败
     */
    public static String WEIXIN_ACESS_ERROR_MSG = "请求微信账号信息失败";

    /**
     * 注册时验证码错误
     */
    public static int TOKEN_ERROR = 413;

    /**
     * 注册时验证码错误
     */
    public static String TOKEN_ERROR_MSG = "操作超时，请重新绑定手机号";

    /**
     * 此用户没有收藏项目(数据不存在:402)
     */
    public static int FAVORITE_NOT_EXIT = 402;

    /**
     * 此用户没有收藏项目
     */
    public static String FAVORITE_NOT_EXIT_MSG = "该用户没有收藏项目";
}
