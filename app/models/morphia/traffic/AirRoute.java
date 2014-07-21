package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import play.data.validation.Constraints;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 航线。
 *
 * @author Zephyre
 */
@Entity
public class AirRoute extends AbstractRoute {
//    @Id
//    public ObjectId id;
//
//    @Constraints.Required
//    @Embedded
//    public SimpleRef depStop;
//
//    @Constraints.Required
//    @Embedded
//    public SimpleRef arrStop;
//
//    @Constraints.Required
//    public String code;
//
//    @Constraints.Required
//    @Embedded
//    public SimpleRef depLoc;
//
//    @Constraints.Required
//    @Embedded
//    public SimpleRef arrLoc;
//
//    public Integer distance;
//
//    @Constraints.Required
//    public Integer timeCost;
//
//    @Constraints.Required
//    public Date depTime;
//
//    @Constraints.Required
//    public Date arrTime;

    @Embedded
    public AirPrice price;

    @Constraints.Required
    @Embedded
    public SimpleRef carrier;

    public Boolean selfChk;

    public Boolean meal;

    public String jetName;

    public String jetFullName;

    public String depTerm;

    public String arrTerm;

    public Boolean nonStop;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", id.toString()).add("code", code);

        for (Map.Entry<String,String> entry: new HashMap<String,String>(){
            {
                put("depStop", "depAirport");
                put("arrStop", "arrAirport");
                put("depLoc", "depLoc");
                put("arrLoc", "arrLoc");
                put("carrier", "carrier");
            }
        }.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            SimpleRef val = null;
            try {
                val = (SimpleRef) AirRoute.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            //PC_Chen:return {} instead of ""
            builder.add(v, val != null ? val.toJson() : new HashMap<>());
        }
        //basic data type fields
        for (String k : new String[]{"distance", "timeCost", "selfChk", "meal", "nonStop"}) {//, "jetName", "jetFullName", "depTerm", "arrTerm"}) {
            Object val = null;
            try {
                val = AirRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            if (val != null)
                builder.add(k, val);
        }
        //String fields
        for (String k : new String[]{"jetName", "jetFullName", "depTerm", "arrTerm"}) {
            Object val = null;
            try {
                val = AirRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            builder.add(k, val != null ? val : "");
        }

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        for (String k : new String[]{"depTime", "arrTime"}) {
            Date val = null;
            try {
                val = (Date) AirRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            builder.add(k, val != null ? fmt.format(val) : "");
        }
        builder.add("price", price != null ? price.toJson() : new HashMap<>());

        // TODO 需要添加dayLag

        return Json.toJson(builder.get());
    }
}
