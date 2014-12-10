package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;


/**
 * 景点信息。
 *
 * @author Zephyre
 */
@JsonFilter("entertainmentFilter")
public class Entertainment extends AbstractPOI {


    @Embedded
    public ViewSpotRatings ratings;

    public String trafficInfo;

    @Override
    public JsonNode toJson(int level) {
        ObjectNode node = (ObjectNode) super.toJson(level);

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        node.putAll((ObjectNode) Json.toJson(builder.get()));
        return node;
    }
}
