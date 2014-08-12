package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.util.ArrayList;

/**
 * 机票的价格信息。
 *
 * @author Zephyre
 */
@Embedded
public class AirPrice implements ITravelPiFormatter {
    public Double discount;

    public Double price;

    public Double tax;

    public Double surcharge;

    public String provider;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"discount", "price", "tax", "surcharge", "provider"}) {
            Object val = null;
            try {
                val = AirPrice.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            //PC_Chen
            if (k.equals("provider")) {
                builder.add(k, val != null ? val : new ArrayList<>());
            } else if (val != null)
                builder.add(k, val);
        }
        return Json.toJson(builder.get());
    }
}
