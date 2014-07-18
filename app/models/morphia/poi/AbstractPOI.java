package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.Contact;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;
import utils.Constants;

import java.lang.reflect.Field;
import java.util.*;

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
    public CheckinRatings ratings;

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

    public static Map<Integer, List<String>> retrievedFields = new HashMap<Integer, List<String>>() {
        {
            put(1, Arrays.asList("name", "addr", "ratings"));
            put(2, Arrays.asList("name", "addr", "ratings", "desc", "imageList", "tags"));
        }
    };

    public JsonNode toJson(int level) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // level1
        builder.add("_id", id.toString());
        builder.add("ratings", (ratings != null ? ratings.toJson() : new BasicDBObject()));
        builder.add("addr", (addr != null ? addr.toJson() : new BasicDBObject()));
        builder.add("name", (name != null ? name : ""));

        // level2
        if (level > 1) {
            for (String k : new String[]{"imageList", "tags"}) {
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
                builder.add(k, (isNull ? new ArrayList<>() : val));
            }
            builder.add("desc", (desc != null ? StringUtils.abbreviate(desc, Constants.ABBREVIATE_LEN) : ""));

            // level3
            if (level > 2) {
                for (String k : new String[]{"url", "price", "priceDesc", "alias"}) {
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
                    builder.add(k, (isNull ? new ArrayList<>() : val));
                }
                builder.add("contact", (contact != null ? contact.toJson() : new HashMap<>()));
                builder.add("desc", (desc != null ? desc : ""));
            }
        }
        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return toJson(3);
    }
}
