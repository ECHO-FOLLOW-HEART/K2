package models.morphia.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import models.Coords;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.List;

/**
 * Locality
 *
 * @author Zephyre
 */
@Entity
public class Locality extends TravelPiBaseItem {
    @Id
    public ObjectId id;

    public String zhName;

    public String enName;

    public List<String> alias;

    public List<String> pinyin;

    public List<Integer> travelMonth;

    @Constraints.Required
    public int level;

    @Version
    public long ver;

    @Reference(lazy = true)
    public Country country;

    @Reference(lazy = true)
    public Locality parent;

    @Reference(lazy = true)
    public List<Locality> siblings;

    public List<String> tags;

    public List<String> imageList;

    public boolean provCap;

    public int baiduId;

    @Embedded
    public Coords coords;

    public String desc;

    public JsonNode getJsonNode() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("name", zhName).add("level", level);

        if (parent != null)
            builder.add("parent", BasicDBObjectBuilder.start().add("_id", parent.id.toString()).add("name", zhName).get());
        else
            builder.add("parent", null);

        BasicDBList sibNodeList = new BasicDBList();
        if (siblings != null) {
            for (Locality sib : siblings)
                sibNodeList.add(BasicDBObjectBuilder.start().add("_id", sib.id.toString()).add("name", sib.zhName).get());
        }
        builder.add("siblings", sibNodeList);

        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return null;
    }
}
