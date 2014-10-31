package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

/**
 * 介绍的类。
 *
 * @author Zephyre
 */
@Embedded
@JsonFilter("descriptionFilter")
public class Description implements ITravelPiFormatter {


    @Transient
    public static String simpDesc = "desc";
    public String desc;
    public String details;
    public String tips;
    public String traffic;

    public String getDesc() {
        if (desc == null)
            return "";
        else
            return desc;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("desc", desc == null ? "" : desc);
        builder.add("details", details == null ? "" : details);
        builder.add("tips", tips == null ? "" : tips);
        builder.add("traffic", traffic == null ? "" : traffic);
        return Json.toJson(builder.get());
    }
}
