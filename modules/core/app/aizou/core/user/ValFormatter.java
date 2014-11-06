package aizou.core.user;

import java.util.Map;

/**
 * 生成验证短信的内容。
 * <p/>
 * Created by zephyre on 14-10-15.
 */
public interface ValFormatter {
    /**
     * 根据提供的信息，生成验证短信内容。
     *
     * @param countryCode 国家代码
     * @param tel         手机号码
     * @param valCode     验证码
     * @param expireMs    过期时间（毫秒）
     * @param misc        其它附加信息
     * @return 验证短信的内容
     */
    public String format(int countryCode, String tel, String valCode, long expireMs, Map<String, Object> misc);
}
