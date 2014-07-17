package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;

import models.TravelPiBaseItem;
import models.morphia.geo.Locality;

import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import play.data.validation.Constraints;
import play.libs.Json;

import java.util.Date;

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
    @Embedded
    public SimpleRef depAirport;

    @Constraints.Required
    @Embedded
    public SimpleRef arrAirport;

    @Constraints.Required
    @Embedded
    public SimpleRef depLoc;

    @Constraints.Required
    @Embedded
    public SimpleRef arrLoc;

    public int distance;

    @Constraints.Required
    public String flightCode;

    @Embedded
    public AirPrice price;

    @Constraints.Required
    public int timeCost;

    @Constraints.Required
    public Date depTime;

    @Constraints.Required
    public Date arrTime;

    @Constraints.Required
    @Embedded
    public SimpleRef carrier;

    public boolean selfChk;

    public boolean meal;

    public String jetName;

    public String jetFullName;

    public String depTerm;

    public String arrTerm;

    public boolean nonStop;

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
