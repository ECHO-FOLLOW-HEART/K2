package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * 机票的价格信息。
 *
 * @author Zephyre
 */
@Embedded
public class AirPrice {
    public Double discount;

    public Double price;

    public Double tax;

    public Double surcharge;

    public String provider;

    public JsonNode toJson(){
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k:new String[]{"discount","price","tax","surcharge","provider"}){
            Object val=null;
            try{
                val=AirPrice.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            builder.add(k,val!=null?val:"");
        }
        return Json.toJson(builder.get());
    }
}
