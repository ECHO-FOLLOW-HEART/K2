package utils;

/**
 * Created by topy on 2014/10/16.
 */
public class MsgConstants {

    /**
     * 用户已存在
     */
    public static int USER_EXIST  = 405;

    /**
     * 用户已存在
     */
    public static String USER_EXIST_MSG  = "该用户已存在.";

    /**
     * 用户不已存在
     */
    public static int USER_NOT_EXIST  = 406;

    /**
     * 用户不已存在
     */
    public static String USER_NOT_EXIST_MSG  = "该用户不存在.";

    /**
     * 注册时验证码错误
     */
    public static int CAPTCHA_ERROR = 403;

    /**
     * 注册时验证码错误
     */
    public static String CAPTCHA_ERROR_MSG = "验证码错误，请重新发送.";

    /**
     * 注册时验证码错误
     */
    public static int PWD_ERROR = 407;

    /**
     * 注册时验证码错误
     */
    public static String PWD_ERROR_MSG = "密码不正确，请重新填写.";
    /**
     * 昵称已存在
     */
    public static int NICKNAME_EXIST  = 408;

    /**
     * 昵称已存在
     */
    public static String NICKNAME_EXIST_MSG  = "该昵称已存在.";

    /**
     * 新设密码格式不正确
     */
    public static int PWD_FORMAT_ERROR = 409;

    /**
     * 新设密码格式不正确
     */
    public static String PWD_FORMAT_ERROR_MSG = "请设置6-10位字母或数字.";

    /**
     * 密码设置失败
     */
    public static int PWD_RESET_ERROR = 410;

    /**
     * 密码设置失败
     */
    public static String PWD_RESET_ERROR_MSG = "密码设置失败.";


}
