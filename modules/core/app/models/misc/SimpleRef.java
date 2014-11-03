package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("simpleRefFilter")
public class SimpleRef implements ITravelPiFormatter {
    public ObjectId id;
    public String enName;
    public String zhName;

    public String getEnName() {
        if (enName == null)
            return "";
        else
            return enName;
    }

    @Override
    public JsonNode toJson() {
        return Json.toJson(BasicDBObjectBuilder.start("_id", id.toString())
                .add("name", (zhName != null ? StringUtils.capitalize(zhName) : ""))
                .add("enName", (enName != null ? StringUtils.capitalize(enName) : "")).get());
    }
}
