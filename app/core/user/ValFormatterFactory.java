package core.user;

import exception.ErrorCode;
import exception.TravelPiException;

/**
 * 验证码内容的工厂类
 *
 * @author Zephyre
 */
public class ValFormatterFactory {
    public static ValFormatter newInstance(int actionCode) throws TravelPiException {
        switch (actionCode) {
            case 1:
                return new SignupValFormatter();
            case 2:
                return new ModPwdValFormatter();
            case 3:
                return new BindTelValFormatter();
            default:
                throw new TravelPiException(ErrorCode.SMS_INVALID_ACTION, String.format("Invalid sms action code: %d.",
                        actionCode));
        }
    }
}
