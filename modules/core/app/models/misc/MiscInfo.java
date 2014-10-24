package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

/**
 * 其它杂项信息。
 *
 * @author Zephyre
 */
@Entity
public class MiscInfo extends TravelPiBaseItem implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    /**
     * 手机首页屏幕的图像。
     */
    public String appHomeImage;

    /**
     * 环信token
     */
    public String easemobToken;

    /**
     * 环信token的过期时间
     */
    public Long easemobTokenExpire;

    /**
     * 环信UUID
     */
    public String easemobUUID;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("appHomeImage", appHomeImage);
        return Json.toJson(builder.get());
    }
}
