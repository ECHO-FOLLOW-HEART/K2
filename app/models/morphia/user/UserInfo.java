package models.morphia.user;

import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

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


    @Override
    public JsonNode toJson() {
        return null;
    }
}
