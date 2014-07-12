package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;

import models.TravelPiBaseItem;
import models.morphia.geo.Locality;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import play.data.validation.Constraints;
import play.libs.Json;

/**
 * 航线。
 *
 * @author Zephyre
 */
@Entity
public class AirRoute extends TravelPiBaseItem{

    @Id
    public ObjectId id;

    @Constraints.Required
    @Reference
    public Airport depAirport;

    @Constraints.Required
    @Reference
    public Airport arrAirport;

    @Constraints.Required
    @Reference
    public Locality depLoc;

    @Constraints.Required
    @Reference
    public Locality arrLoc;

    public int distance;

    public String flightCode;

    @Override
    public JsonNode toJson() {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", id).add("distance", distance).add("flightCode", flightCode);
    	if(depAirport != null){
    		builder.add("depAirport", 
    				BasicDBObjectBuilder.start().add("_id",depAirport.id)
    				.add("name", depAirport.zhName));
    	}
    	if(arrAirport != null){
    		builder.add("arrAirport", 
    				BasicDBObjectBuilder.start().add("_id",arrAirport.id)
    				.add("name", arrAirport.zhName));
    	}
    	if(depLoc != null){
    		builder.add("depLoc", 
    				BasicDBObjectBuilder.start().add("_id",depLoc.id)
    				.add("name", depLoc.zhName));
    	}
    	if(arrLoc != null){
    		builder.add("arrLoc", 
    				BasicDBObjectBuilder.start().add("_id",arrLoc.id)
    				.add("name", arrLoc.zhName));
    	}
        return Json.toJson(builder.get());
    }
}
