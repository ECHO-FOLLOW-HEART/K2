package models.backup.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.geo.Country;
import play.libs.Json;

/**
 * 将国家模型输出为JsonNode
 *
 * @author Zephyre
 */
public class CountryFormatter {
    public JsonNode toJson(Country c) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", c.code).add("zhName", c.zhName).add("enName", c.enName).add("code3", c.code3);
        return Json.toJson(builder.get());
    }
}
