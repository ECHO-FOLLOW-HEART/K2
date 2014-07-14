package models.morphia.poi;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 景点信息。
 *
 * @author Zephyre
 */
public class ViewSpot extends TravelPiBaseItem {
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

    public Boolean worldHeritage;

    public Integer spotId;

    /**
     * AAA景区：3
     * AAAA景区：4
     */
    public Integer rankingA;

    public String openTime;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
