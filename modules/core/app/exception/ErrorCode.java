package exception;

/**
 * 错误代码
 *
 * @author Zephyre
 */
public enum ErrorCode {
    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 输入参数错误
     */
    INVALID_ARGUMENT(100),
    /**
     * 通用IO错误
     */
    IO_ERROR(101),
    /**
     * 数据库错误
     */
    DATABASE_ERROR(102),

    /**
     * 数据已存在的错误
     */
    DATA_EXIST(401),

    /**
     * 数据不存在的错误
     */
    DATA_NOT_EXIST(402),

    /**
     * 注册时验证码错误
     */
    CAPTCHA_ERROR(403),

    /**
     * 微信注册时请求用户信息失败
     */
    WEIXIN_CODE_ERROR(404),
    /**
     * 用户已存在
     */
    USER_EXIST(405),

    /**
     * 用户不已存在
     */
    USER_NOT_EXIST(406),

    /**
     * 登录验证失败
     */
    AUTH_ERROR(407),

    /**
     * 短信发送超过配额
     */
    SMS_QUOTA_ERROR(500),

    /**
     * 无效的短信操作码
     */
    SMS_INVALID_ACTION(501),

    /**
     * 搜索引擎通用错误
     */
    SEARCH_ENGINE_ERROR(600),

    /**
     * 未知错误
     */
    UNKOWN_ERROR(900);

    private final int val;

    ErrorCode(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
