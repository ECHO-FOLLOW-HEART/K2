package models.morphia.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.List;
import java.util.Map;

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
public class UserInfo extends TravelPiBaseItem implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    /**
     * 昵称
     */
    @Constraints.Required
    public String nickName;

    /**                                s
     * 头像
     */
    @Constraints.Required
    public String avatar;

    /**
     * 性别： F\M
     */
    public String gender;
    /**
     * 签名
     */
    public String signature;
    /**
     * 手机号
     */
    public String tel;
    /**
     * 国家编码
     */
    public Integer countryCode;
    /**
     * 用户ID
     */
    public Integer userId;

    /**
     * 好友列表:用户ID-用户简要信息
     */
    public Map<Integer, UserInfo> friends;

    /**
     * 好友备注:用户ID-备注信息
     */
    public Map<Integer,String> remark;

    /**
     * 黑名单：用户ID-用户简要信息
     */
    public Map<Integer, UserInfo> blackList;

    /**
     * 邮箱
     */
    public String email;

    /**
     * 第三方OAuth登录信息
     */
    public List<OAuthInfo> oauthList;

    /**
     * 来源
     */
    public String origin;

    /**
     * 唯一设备号。
     */
    public String udid;

    /**
     * 用户手机系统及版本
     *
     * @return
     */
    public String platform;

    /**
     * 用户App版本
     *
     * @return
     */
    public String appVersion;

    /**
     * 用户令牌
     */
    public String secToken;




    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("nickName", nickName).add("avatar", avatar).add("secToken", secToken);

        return Json.toJson(builder.get());
    }
}
