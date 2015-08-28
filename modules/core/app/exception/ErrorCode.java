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
     * Http请求中输入参数错误
     */
    INVALID_ARGUMENT(100),
    /**
     * Http请求中缺少参数
     */
    LACK_OF_ARGUMENT(101),
    /**
     * 缺少相应的权限
     */
    LACK_OF_AUTH(102),
    /**
     * 通用IO错误
     */
    IO_ERROR(110),
    /**
     * 数据库错误
     */
    DATABASE_ERROR(111),
    /**
     * 搜索引擎通用错误
     */
    SEARCH_ENGINE_ERROR(103),
    /**
     * Yunkai工程中的Exception对应的ErrorCode
     */
    // 用户不存在
    YUNKAI_USER_NOT_FOUND(200),
    // Yunkai的非法参数错误
    YUNKAI_INVALIDARGS(201),
    // 登录验证失败
    YUNKAI_AUTH_ERROR(202),
    // 用户已存在
    YUNKAI_USEREXISTS(203),
    // 资源冲突
    YUNKAI_RESOURCECONFLICT(204),
    //群组成员限制
    YUNKAI_GROUPMEMBERSLIMIT(205),
    // 非法状态
    YUNKAI_INVALIDSTATE(206),
    // 验证码错误
    YUNKAI_VALIDATIONCODE(207),
    //
    YUNKAI_OVERQUOTALIMIT(208),

    /**
     * 短信发送超过配额
     */
    SMS_QUOTA_ERROR(210),
    /**
     * 旧密码错误
     */
    OLD_PWD_ERROR(214),
    /**
     * 第三方授权登录时，授权失败
     */
    THPART_AUTH_ERROR(215),
    /**
     * 群組不存在
     */
    GROUP_NOT_EXIST(216),
    /**
     * 手机号已存在
     */
    TEL_EXIST(217),

    /**
     * 重复申请达人
     */
    MUL_REQUEST_EXPERT(230),


    /**
     * Hedylogs工程中的Exception对应的ErrorCode
     */
    HEDY_BASE_EXP(300),

    HEDY_BLACKLIST_EXP(301),

    HEDY_CONTACT_EXP(302),

    HEDY_GROUPMEMBER_EXP(303),

    HEDY_REDIS_EXP(304);

    private final int val;

    ErrorCode(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
