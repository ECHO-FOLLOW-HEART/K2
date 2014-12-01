package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * 推荐类型。
 *
 * @author Zephyre
 */
@JsonFilter("recomTypeFilter")
@Embedded
public class RecomType extends TravelPiBaseItem {
    @Transient
    public static String fnId = "id";
    @Transient
    public static String fnName = "name";

    public String name;

}
