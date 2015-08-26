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
     * 未知错误
     */
    UNKOWN_ERROR(900),
    /** K2 系统性 */
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
     * 搜索引擎通用错误
     */
    SEARCH_ENGINE_ERROR(103),
    /**
     * Yunkai工程中的Exception对应的ErrorCode
     */
    YUNKAI_NOTFOUND(200),
    YUNKAI_INVALIDARGS(201),
    YUNKAI_AUTH(202),
    YUNKAI_USEREXISTS(203),
    YUNKAI_RESOURCECONFLICT(204),
    YUNKAI_GROUPMEMBERSLIMIT(205),
    YUNKAI_INVALIDSTATE(206),
    YUNKAI_VALIDATIONCODE(207),
    YUNKAI_OVERQUOTALIMIT(208),
    /**
     * Hedylogs工程中的Exception对应的ErrorCode
     */
    HEDY_BASE_EXP(300),
    HEDY_BLACKLIST_EXP(301),
    HEDY_CONTACT_EXP(302),
    HEDY_GROUPMEMBER_EXP(303),
    HEDY_REDIS_EXP(304),
    /**
     * 数据已存在的错误
     */
    DATA_EXIST(401),

    /**
     * 注册时验证码错误
     */
    CAPTCHA_ERROR(403),

    /**
     * 数据不存在的错误
     */
    DATA_NOT_EXIST(404),

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
    SMS_QUOTA_ERROR(410),

    /**
     * 无效的短信操作码
     */
    SMS_INVALID_ACTION(411);
    private final int val;

    ErrorCode(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
