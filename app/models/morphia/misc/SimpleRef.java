package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 去正规化的简要引用。
 *
 * @author Zephyre
 */
@Embedded
public class SimpleRef implements ITravelPiFormatter {
    public ObjectId id;
    public String enName;
    public String zhName;

    @Override
    public JsonNode toJson() {
        return Json.toJson(BasicDBObjectBuilder.start().
                 add("_id", id.toString())
                .add("name", (zhName != null ? StringUtils.capitalize(zhName) : ""))
                .add("enName", (enName != null ? StringUtils.capitalize(enName) : ""))
                .get());
    }
}
