package models.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by topy on 2014/9/29.
 */
@Entity
public class BusRoute extends AbstractRoute {

    public Double price;

    @Override
    public JsonNode toJson() {

        ObjectNode node = (ObjectNode) super.toJson();

        if (price != null)
            node.put("price", price.toString());

        return node;
    }
}
