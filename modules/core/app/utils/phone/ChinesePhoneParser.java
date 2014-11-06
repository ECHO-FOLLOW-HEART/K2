package utils.phone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析中国的电话号码
 *
 * @author Zephyre
 */
class ChinesePhoneParser implements PhoneParser {
    @Override
    public PhoneEntity parse(String text) throws IllegalArgumentException {
        // 去除所有空格

        text = Pattern.compile("\\s+").matcher(text).replaceAll("");

        if (Pattern.compile("^(\\+|00)").matcher(text).find()) {
            Matcher m = Pattern.compile("^(\\+|00)86(.+)").matcher(text);
            if (!m.find())
                throw new IllegalArgumentException();
            else
                text = m.group(2);
        }

        int dialCode = 86;

        String tel = null;

        Matcher m = Pattern.compile("[\\.\\d\\-]+").matcher(text);
        while (m.find()) {
            String component = m.group();

            StringBuilder sb = new StringBuilder();
            Matcher m1 = Pattern.compile("\\d+").matcher(component);
            while (m1.find()) {
                sb.append(m1.group());
            }
            String tmp = sb.toString();

            // 判断
            if (tmp.length() == 11 && tmp.charAt(0) == '1') {
                tel = tmp;
                break;
            }
        }

        if (tel == null)
            throw new IllegalArgumentException();

        PhoneEntity phone = new PhoneEntity();
        phone.setCellPhone(true);
        phone.setDialCode(dialCode);
        phone.setPhoneNumber(tel);
        return phone;
    }
}
