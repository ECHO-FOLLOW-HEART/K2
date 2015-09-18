package models.misc;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 热门搜索
 *
 * Created by topy on 2014/9/13.
 */
@Entity
public class HotSearch extends AizouBaseEntity {

    @Transient
    public static String fnId = "id";
    @Transient
    public static String fnIitemType = "itemType";
    @Transient
    public static String fnItemId = "itemId";
    @Transient
    public static String fnZhName = "zhName";
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

    /**
     * 推荐分类
     */
    public String itemType;

    public String scope;

    /**
     * 推荐分类
     */
    public String zhName;

    /**
     * 推荐项目的类型
     */
    public ObjectId itemId;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
