package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.List;

/**
 * 用户的收藏夹
 * <p>
 * Created by topy on 2014/10/27.
 */
@Entity
@JsonFilter("favoriteFilter")
public class Favorite extends TravelPiBaseItem {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnItemId = "itemId";


    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnType = "type";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnImage = "images";

    @Transient
    public static String fnCreateTime = "createTime";

    @Transient
    public static String TYPE_VS = "vs";

    @Transient
    public static String TYPE_HOTEL = "hotel";

    @Transient
    public static String TYPE_RESTAURANT = "restaurant";

    @Transient
    public static String TYPE_SHOPPING = "shopping";

    @Transient
    public static String TYPE_ENTERTAINMENT = "entertainment";

    @Transient
    public static String TYPE_TRAVELNOTE = "travelNote";

    @Transient
    public static String TYPE_LOCALITY = "locality";

    @Transient
    public static String fnDesc = "desc";

    /**
     * 用户ID
     */
    @Constraints.Required
    public Integer userId;

    public ObjectId itemId;

    public String type;

    public String zhName;

    public String enName;

    public String desc;

    public List<ImageItem> images;

    public Date createTime;

    public String getItemId() {
        return itemId.toString();
    }

    public String getEnName() {
        return enName == null ? "" : enName;
    }

}
