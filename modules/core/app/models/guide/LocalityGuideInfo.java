package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 目的地与攻略相关的信息
 * <p/>
 * Created by topy on 2014/11/28.
 */
@JsonFilter("localityGuideInfoFilter")
public class LocalityGuideInfo extends AizouBaseEntity {

    @Transient
    public static final String fnShoppingImages = "shoppingImages";

    @Transient
    public static final String fnShoppingDesc = "shoppingDesc";

    @Transient
    public static final String fnRestaurantImages = "restaurantImages";

    @Transient
    public static final String fnRestaurantDesc = "restaurantDesc";

    public List<ImageItem> shoppingImages;

    public String shoppingDesc;

    public List<ImageItem> restaurantImages;

    public String restaurantDesc;

    public ObjectId locId;

    public String getLocId() {
        return locId.toString();
    }

}
