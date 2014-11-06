package aizou.core.user;

import java.util.Map;

/**
 * 生成注册时的验证码内容。
 *
 * @author Zephyre
 */
public class BindTelValFormatter implements ValFormatter {

    @Override
    public String format(int countryCode, String tel, String valCode, long expireMs, Map<String, Object> misc) {
        return String.format("%s（桃子旅行手机绑定验证码），请在%d分钟内完成手机绑定。如非本人操作，请忽略。【桃子旅行】", valCode,
                expireMs / 60000);
    }
}
