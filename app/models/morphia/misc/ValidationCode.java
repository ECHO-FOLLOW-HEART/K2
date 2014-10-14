package models.morphia.misc;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.utils.IndexDirection;
import play.data.validation.Constraints;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 存储验证码。
 *
 * @author Zephyre
 */
@Entity
public class ValidationCode {

    /**
     * 实例化一个验证码对象。
     *
     * @param countryCode 国家代码，默认为86。
     * @param tel         电话号码
     * @return 一个新的验证码对象
     */
    public static ValidationCode newInstance(Integer countryCode, String tel) {
        ValidationCode code = new ValidationCode();

        code.key = calcKey(countryCode, tel);
        code.value = ((Integer) (new Random()).nextInt(1000000)).toString();
        code.createTime = System.currentTimeMillis();
        // 默认1小时过期
        code.expireTime = code.createTime + 60 * 60 * 1000L;
        code.countryCode = 86;
        code.tel = tel;

        return code;
    }

    /**
     * 根据国家代码和手机号获得key。
     *
     * @param countryCode
     * @param tel
     * @return
     */
    public static String calcKey(Integer countryCode, String tel) {
        String fullName = String.format("+%d%s", countryCode, tel);

        try {
            return Base64.encodeBase64String(MessageDigest.getInstance("MD5").digest(fullName.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
    }

    private ValidationCode() {
    }

    @Id
    public ObjectId id;

    /**
     * 验证码的内容
     */
    @Constraints.Required
    public String value;

    /**
     * 验证码的key。由国家代码+手机号经过hash生成，方便查询。
     */
    @Indexed(value = IndexDirection.ASC, unique = true, dropDups = true)
    @Constraints.Required
    public String key;

    /**
     * 过期时间
     */
    @Indexed(value = IndexDirection.ASC)
    @Constraints.Required
    public Long expireTime;

    /**
     * 验证码的生成时间
     */
    @Constraints.Required
    public Long createTime;

    /**
     * 发送验证码的时间
     */
    public Long sendTime;

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
}
