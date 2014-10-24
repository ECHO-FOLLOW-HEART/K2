package exception;

/**
 * 错误代码
 *
 * @author Zephyre
 */
public class ErrorCode {
    /**
     * 正常
     */
    public static int NORMAL = 0;

    /**
     * 输入参数错误
     */
    public static int INVALID_ARGUMENT = 100;

    /**
     * 路线需要修改
     */
    public static int DONOTNEED_UPDATE = 120;


    public static int INVALID_CONFARG = 130;

    /**
     * 数据库错误
     */
    public static int DATABASE_ERROR = 200;

    /**
     *
     */
    public static int UNSUPPORTED_OP = 800;

    /**
     * 未知错误
     */
    public static int UNKOWN_ERROR = 900;

    public static int AUTHENTICATE_ERROR = 300;

    /**
     * 数据已存在的错误
     */
    public static int DATA_EXIST = 401;

    public static int DATA_NOT_EXIST = 402;

    /**
     * 注册时验证码错误
     */
    public static int CAPTCHA_ERROR = 403;

    /**
     * 微信注册时请求用户信息失败
     */
    public static int WEIXIN_CODE_ERROR = 404;

    /**
     * 用户已存在
     */
    public static int USER_EXIST = 405;

    /**
     * 用户不已存在
     */
    public static int USER_NOT_EXIST = 406;

    /**
     * 短信发送超过配额
     */
    public static int SMS_QUOTA_ERROR = 500;

    /**
     * 无效的短信操作码
     */
    public static int SMS_INVALID_ACTION = 501;

}
