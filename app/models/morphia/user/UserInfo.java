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

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
public class UserInfo extends TravelPiBaseItem implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    @Constraints.Required
    public String nickName;

    /**
     * 头像
     */
    @Constraints.Required
    public String avatar;

    /**
     * 第三方OAuth登录信息
     */
    public List<OAuthInfo> oauthList;

    /**
     * 唯一设备号。
     */
    public String udid;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("nickName", nickName).add("avatar", avatar);
        return Json.toJson(builder.get());
    }
}
