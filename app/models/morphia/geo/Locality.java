package models.morphia.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import models.TravelPiBaseItem;
import models.morphia.misc.Ratings;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.ArrayList;
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

    @Indexed()
    public String zhName;

    public String enName;

    public List<String> alias;

    public List<String> pinyin;

    public List<Integer> travelMonth;

    @Constraints.Required
    public int level;

    @Embedded
    public Ratings ratings;

    public String countryId;

    public String countryEnName;

    public String countryZhName;

    @Embedded
    public SimpleRef superAdm;

    @Embedded
    public List<SimpleRef> sib;

//    @Reference(lazy = true)
//    public List<Locality> siblings;

    public List<String> tags;

    public List<String> imageList;

    public boolean provCap;

    public Integer baiduId;

    public Integer qunarId;

    @Embedded
    public Coords coords;

    public String desc;

    public JsonNode getJsonNode() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("name", zhName).add("level", level);

        builder.add("parent", null);

        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return toJson(1);
    }

    public JsonNode toJson(int detailLevel) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("name", zhName).add("level", level);

        if (superAdm != null)
            builder.add("parent", superAdm.toJson());
        else
            builder.add("parent", new BasicDBObject());

        if (coords != null) {
            if (coords.blat != null) builder.add("blat", coords.blat);
            if (coords.blng != null) builder.add("blng", coords.blng);
            if (coords.lat != null) builder.add("lat", coords.lat);
            if (coords.lng != null) builder.add("lng", coords.lng);
        }

        if (detailLevel > 1) {
            builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");

            if (ratings != null)
                builder.add("ratings", ratings.toJson());
            else
                builder.add("ratings", new BasicDBObject());

            builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? imageList : new ArrayList<>());
            builder.add("tags", (tags != null && !tags.isEmpty()) ? tags : new ArrayList<>());
        }

        return Json.toJson(builder.get());
    }
}
