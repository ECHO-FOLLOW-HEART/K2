package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.AizouBaseItem;
import models.ITravelPiFormatter;
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
@JsonFilter("simpleRefFilter")
@Embedded
public class SimpleRef extends AizouBaseEntity implements ITravelPiFormatter {
    @Transient
    public static String simpID = "id";
    @Transient
    public static String simpEnName = "enName";
    @Transient
    public static String simpZhName = "zhName";
    public ObjectId id;

    public String enName;

    public String zhName;

    public String getEnName() {
        if (enName == null)
            return "";
        else
            return StringUtils.capitalize(enName);
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getZhName() {
        if (zhName == null)
            return "";
        else
            return StringUtils.capitalize(zhName);
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    @Override
    public JsonNode toJson() {
        return Json.toJson(BasicDBObjectBuilder.start("_id", id.toString())
                .add("name", (zhName != null ? StringUtils.capitalize(zhName) : ""))
                .add("enName", (enName != null ? StringUtils.capitalize(enName) : "")).get());
    }
}
