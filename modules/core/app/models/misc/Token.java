package models.misc;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.utils.IndexDirection;
import utils.Utils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by topy on 2014/10/21.
 */
@Entity
public class Token {

    @Id
    public ObjectId id;

    /**
     * 验证码的内容
     */
    @NotNull
    public String value;

    /**
     * 验证码的内容
     */
    public Long userId;

    /**
     * 操作权限:1-注册,2-修改密码,3-绑定
     */
    public List<Integer> permissionList;

    /**
     * 过期时间
     */
    @Indexed(value = IndexDirection.ASC)
    @NotNull
    public Long expireTime;

    /**
     * 验证码的生成时间
     */
    @NotNull
    public Long createTime;

    /**
     * 已经使用
     */
    public boolean used;


    /**
     * 实例化一个令牌对象。
     *
     * @return 一个新的验证码对象
     */
    public static Token newInstance() {
        Token code = new Token();
        code.value = Utils.createToken();
        code.createTime = System.currentTimeMillis();
        code.expireTime = code.createTime + 100000;
        code.permissionList = new ArrayList<>();
        return code;
    }

    /**
     * 实例化一个令牌对象。
     *
     * @return 一个新的验证码对象
     */
    public static Token newInstance(ValidationCode v, long expireMs) {
        Token code = new Token();
        code.value = Utils.createToken();
        code.createTime = System.currentTimeMillis();
        code.expireTime = code.createTime + expireMs;
        code.userId = v.userId;
        code.permissionList = v.permissionList;
        return code;
    }
}
