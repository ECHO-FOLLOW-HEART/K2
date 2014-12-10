package models.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import models.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;
import play.libs.Json;

/**
 * 航空公司。
 *
 * @author Zephyre
 */
@Entity
public class Airline extends AizouBaseEntity implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    @Constraints.Required
    public String code;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String fullName;

    public String shortName;

    @Embedded
    public Contact contact;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", id)
                .add("code", code).add("name", name)
                .add("fullName", fullName).add("shortName", shortName);

        return Json.toJson(builder.get());
    }
}
