package models.misc;

import org.apache.commons.codec.binary.Base64;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.utils.IndexDirection;
import play.data.validation.Constraints;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 存储验证码。
 *
 * @author Zephyre
 */
@Entity
public class ValidationCode extends Token {

    /**
     * 验证码的key。由国家代码+手机号+操作码经过hash生成，方便查询。
     */
    @Indexed(value = IndexDirection.ASC, unique = true, dropDups = true)
    @Constraints.Required
    public String key;
    /**
     * 国家代码
     */
    @Constraints.Required
    public Integer countryCode;
    /**
     * 手机号码
     */
    @Constraints.Required
    public String tel;
    /**
     * 上次发送验证码的时间
     */
    public Long lastSendTime;
    /**
     * 下次可以发送验证码的时间
     */
    public Long resendTime;
    /**
     * 验证失败的次数
     */
    public Integer failCnt;

    private ValidationCode() {
    }

    /**
     * 实例化一个验证码对象。
     *
     * @param countryCode 国家代码，默认为86。
     * @param tel         电话号码
     * @param actionCode  操作码
     * @return 一个新的验证码对象
     */
    public static ValidationCode newInstance(int countryCode, String tel, int actionCode, Integer userId, long expireMs) {
        ValidationCode code = new ValidationCode();

        code.key = calcKey(countryCode, tel, actionCode);
        code.value = ((Integer) (new Random()).nextInt(1000000)).toString();
        code.createTime = System.currentTimeMillis();
        code.expireTime = code.createTime + expireMs;
        code.countryCode = 86;
        code.tel = tel;
        List<Integer> pList = new ArrayList<Integer>();
        pList.add(actionCode);
        code.permissionList = pList;
        code.failCnt = 0;
        code.userId = userId;

        return code;
    }

    /**
     * 根据国家代码、手机号和操作码获得key。
     *
     * @param countryCode 国家代码
     * @param tel         手机号码
     * @param actionCode  操作码
     * @return
     */
    public static String calcKey(int countryCode, String tel, int actionCode) {
        String fullName = String.format("+%d%s%d", countryCode, tel, actionCode);
        try {
            return Base64.encodeBase64String(MessageDigest.getInstance("MD5").digest(fullName.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
    }

}
