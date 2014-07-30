package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.Contact;
import models.morphia.misc.Description;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Embedded;
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
public abstract class AbstractPOI extends TravelPiBaseItem implements ITravelPiFormatter {
//    @Id
//    public ObjectId id;

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

    @Embedded
    public Description description;

    public String trafficInfo;

    public List<String> imageList;

    public List<String> tags;

    public List<String> alias;

    public static List<String> getRetrievedFields(int level) {
        switch (level) {
            case 1:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings"));
            case 2:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings", "desc", "imageList", "tags"));
            case 3:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings", "desc", "imageList", "tags", "contact", "url",
                        "price", "priceDesc", "alias"));
        }
        return new ArrayList<>();
    }

    public JsonNode toJson(int level) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // level1
        builder.add("_id", id.toString());
        builder.add("ratings", (ratings != null ? ratings.toJson() : new BasicDBObject()));
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
            if (price != null)
                builder.add("price", price);

            // level3
            if (level > 2) {
                builder.add("url", url != null ? url : "");
                builder.add("priceDesc", priceDesc != null ? priceDesc : "");
                builder.add("alias", alias != null ? alias : new ArrayList<>());

                builder.add("contact", (contact != null ? contact.toJson() : new HashMap<>()));
                builder.add("desc", (desc != null ? desc : ""));
            }
        }

        if (level == 1)
            builder.add("addr", (addr != null ? addr.toJson(1) : new BasicDBObject()));
        else if (level > 1)
            builder.add("addr", (addr != null ? addr.toJson(3) : new BasicDBObject()));

        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return toJson(3);
    }
}
