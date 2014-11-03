package utils.phone;

/**
 * 处理电话号码
 * <p/>
 * Created by zephyre on 10/30/14.
 */
public class PhoneParserFactory {
    public static PhoneParser newInstance() {
        return new ChinesePhoneParser();
    }
}
