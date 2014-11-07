package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import models.misc.Description;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.util.Arrays;
import java.util.List;


/**
 * 景点信息。
 *
 * @author Zephyre
 */
@JsonFilter("shoppingFilter")
public class Shopping extends AbstractPOI {

    public Integer spotId;

    public String trafficInfo;

    @Embedded
    public ViewSpotRatings ratings;

    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        node.putAll((ObjectNode) Json.toJson(builder.get()));
        return node;
    }
}
