package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;
import models.morphia.misc.Ratings;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * POI的基类
 *
 * @author Zephyre
 *         Created by zephyre on 7/16/14.
 */
public abstract class AbstractPOI extends TravelPiBaseItem {
    @Id
    public ObjectId id;

    @Embedded
    public Ratings ratings;

    @Embedded
    public Contact contact;

    @Embedded
    public Address addr;

    public String name;

    public String url;

    public Double price;

    public String priceDesc;

    public String desc;

    public List<String> imageList;

    public List<String> tags;

    public List<String> alias;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        builder.add("id", id.toString());

        builder.add("ratings", (ratings != null ? ratings.toJson() : ""));

        builder.add("contact", (contact != null ? contact.toJson() : ""));

        builder.add("addr", (addr != null ? addr.toJson() : ""));

        for (String k : new String[]{"name", "url", "price", "priceDesc", "desc", "imageList", "tags", "alias"}) {
            Field field;
            Object val = null;
            try {
                field = AbstractPOI.class.getField(k);
                val = field.get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            boolean isNull = (val == null);
            if (val != null && val instanceof Collection)
                isNull = ((Collection) val).isEmpty();
            builder.add(k, (isNull ? "" : val));
        }

        return Json.toJson(builder.get());
    }
}
