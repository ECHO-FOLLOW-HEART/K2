package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
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
public class MiscInfo extends TravelPiBaseItem{

    @Id
    public ObjectId id;

    /**
     * 手机首页屏幕的图像。
     */
    public String appHomeImage;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("appHomeImage", appHomeImage);
        return Json.toJson(builder.get());
    }
}
