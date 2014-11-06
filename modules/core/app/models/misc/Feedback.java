package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by topy on 2014/8/20.
 */
public class Feedback extends TravelPiBaseItem implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    public Object uid;

    public String body;

    public Date time;

    public List<Contact> contact;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("uid", uid.toString()).add("body", body.toString());
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        builder.add("time", time == null ? "" : fmt.format(time));
        List<JsonNode> contactNodesList = new ArrayList<JsonNode>(10);
        if (contact != null && !contact.isEmpty()) {
            for (Contact entry : contact) {
                contactNodesList.add(entry.toJson());
            }
            builder.add("details", Json.toJson(contactNodesList));
        }

        return Json.toJson(builder.get());
    }
}
