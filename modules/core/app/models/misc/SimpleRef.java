package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

/**
 * 去正规化的简要引用。
 *
 * @author Zephyre
 */
@Embedded
@JsonFilter("simpleRfFilter")
public class SimpleRef extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static String simpEnName="enName";
    @Transient
    public static String simpZhName="zhName";
    @Transient
    public static String simpId="id";
    public ObjectId id;
    public String enName;
    public String zhName;

    public String getEnName(){
        if (enName==null)
            return "";
        else
            return StringUtils.capitalize(enName);
    }

    public String getZhName(){
        if (zhName==null)
            return "";
        else
            return StringUtils.capitalize(zhName);
    }
    public String getId(){
        return id.toString();
    }
    @Override
    public JsonNode toJson() {
        return Json.toJson(BasicDBObjectBuilder.start("_id", id.toString())
                .add("name", (zhName != null ? StringUtils.capitalize(zhName) : ""))
                .add("enName", (enName != null ? StringUtils.capitalize(enName) : "")).get());
    }
}
