package models.misc;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2015/9/24.
 */
public class Reference extends AizouBaseEntity {

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_EN_NAME = "enName";

    @Transient
    public static final String FD_ITEMID = "itemId";

    @Transient
    public static final String FD_ISABROAD = "isAbroad";

    @Transient
    public static final String FD_ITEMTYPE = "itemType";

    @Transient
    public static final String FD_IMAGES = "images";

    @Transient
    public static String TYPE_VS = "viewspot";

    @Transient
    public static String TYPE_HOTEL = "hotel";

    @Transient
    public static String TYPE_RESTAURANT = "restaurant";

    @Transient
    public static String TYPE_SHOPPING = "shopping";

    @Transient
    public static String TYPE_TRAVELNOTE = "travelNote";

    @Transient
    public static String TYPE_LOCALITY = "locality";

    private ObjectId itemId;

    private String zhName;

    private String enName;

    private List<ImageItem> images;

    private String itemType;

    private Boolean isAbroad;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public Boolean isAbroad() {
        return isAbroad;
    }

    public void setIsAbroad(Boolean isAbroad) {
        this.isAbroad = isAbroad;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }
}
