package models.morphia.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

/**
 * 国家模型。
 *
 * @author Zephyre
 */
@Entity
public class Country extends TravelPiBaseItem {
    @Id
    public String code;

    public String code3;

    public String zhName;

    public String enName;

    public String defCurrency;

    public Country(String code, String zhName) {
        this.code = code;
        this.zhName = zhName;
    }

    public Country() {
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("code", code).add("zhName", zhName).add("enName", enName).add("code3", code3);
        return Json.toJson(builder.get());
    }
}
