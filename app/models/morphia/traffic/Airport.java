package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import models.Address;
import models.TravelPiBaseItem;
import models.morphia.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 机场。
 *
 * @author Zephyre
 */
@Entity
public class Airport extends TravelPiBaseItem {

    @Id
    public ObjectId id;

    @Embedded
    public Address address;

    public String zhName;

    public String enName;

    public String url;

    public String desc;

    @Embedded
    public Contact contact;

    public List<String> alias;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
