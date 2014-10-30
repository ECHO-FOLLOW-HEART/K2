package utils.phone;


/**
 * 解析电话号码
 *
 * @author Zephyre
 */
public interface PhoneParser {
    /**
     * 将一段文字转换成电话号码
     */
    public PhoneEntity parse(String text) throws IllegalArgumentException;
}
