package models.morphia.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBList;
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
            //PC_Chen:
//            builder.add("blat", (coords.blat != null ? coords.blat : ""));
//            builder.add("blng", (coords.blng != null ? coords.blng : ""));
//            builder.add("lat", (coords.lat != null ? coords.lat : ""));
//            builder.add("lng", (coords.lng != null ? coords.lng : ""));
            if (coords.blat != null) builder.add("blat", coords.blat);
            if (coords.blng != null) builder.add("blng", coords.blng);
            if (coords.lat != null) builder.add("lat", coords.lat);
            if (coords.lng != null) builder.add("lng", coords.lng);

        }

        if (detailLevel > 1) {
            builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");

            if (ratings != null) {
                BasicDBObjectBuilder rBuilder = BasicDBObjectBuilder.start();
                for (String k : new String[]{"dinningIdx", "shoppingIdx", "score", "favorCnt", "viewCnt"}) {
                    Object tmp = null;
                    try {
                        tmp = Ratings.class.getField(k).get(ratings);
                    } catch (IllegalAccessException | NoSuchFieldException ignored) {
                    }
                    if (tmp != null)
                        rBuilder.add(k, tmp);
                    //PC_Chen:
//                    else
//                        rBuilder.add(k, "");
                }
                builder.add("ratings", rBuilder.get());
            } else
                builder.add("ratings", new BasicDBObject());

            BasicDBList imageListNodes = new BasicDBList();
            if (imageList != null) {
                for (String url : imageList)
                    imageListNodes.add(url);
            }
            builder.add("imageList", imageListNodes);

            BasicDBList tagsNodes = new BasicDBList();
            if (tags != null) {
                for (String tag : tags)
                    tagsNodes.add(tag);
            }
            builder.add("tags", tagsNodes);
        }

        return Json.toJson(builder.get());
    }
}
