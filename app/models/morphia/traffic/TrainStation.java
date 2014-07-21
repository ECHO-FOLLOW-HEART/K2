package models.morphia.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 机场。
 *
 * @author Zephyre
 */
@Entity
public class TrainStation extends TravelPiBaseItem implements ITravelPiFormatter {

    @Id
    public ObjectId id;

    @Embedded
    public Address addr;

    public String zhName;

    public String enName;

    public String url;

    public String desc;

    @Embedded
    public Contact contact;

    public List<String> alias;

    /**
     * 拼音
     */
    public List<String> py;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("name", zhName).add("url", url).add("alias", alias);

        BasicDBList phoneList = new BasicDBList();
        if (contact != null && contact.phoneList != null) {
            for (String val : contact.phoneList)
                phoneList.add(val);
        }
        builder.add("tel", !phoneList.isEmpty() ? phoneList : new ArrayList<>());

        builder.add("alias", (alias != null && !alias.isEmpty()) ? alias : new ArrayList<>());

        if (addr != null) {
            BasicDBObjectBuilder addressBuilder = BasicDBObjectBuilder.start();
            addressBuilder.add("addr", (addr.address != null ? addr.address : ""));
            if (addr.loc != null) {
                addressBuilder.add("loc", BasicDBObjectBuilder.start().add("_id", addr.loc.id).add("name", addr.loc.zhName));
            }
            if (addr.coords != null) {
                BasicDBObjectBuilder coordsBuilder = BasicDBObjectBuilder.start();
                //PC_Chen
                if (addr.coords.blat != null) coordsBuilder.add("blat", addr.coords.blat);
                if (addr.coords.blng != null) coordsBuilder.add("blng", addr.coords.blng);
                if (addr.coords.lat != null) coordsBuilder.add("lat", addr.coords.lat);
                if (addr.coords.lng != null) coordsBuilder.add("lng", addr.coords.lng);
//                coordsBuilder.add("blat", (addr.coords.blat != null ? addr.coords.blat : ""));
//                coordsBuilder.add("blng", (addr.coords.blng != null ? addr.coords.blng : ""));
//                coordsBuilder.add("lat", (addr.coords.lat != null ? addr.coords.lat : ""));
//                coordsBuilder.add("lng", (addr.coords.lng != null ? addr.coords.lng : ""));

                addressBuilder.add("coords", coordsBuilder.get());
            }
            builder.add("addr", addressBuilder);
        }
        return Json.toJson(builder.get());
    }
}