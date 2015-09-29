package models.misc;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 热门搜索
 * <p>
 * Created by topy on 2014/9/13.
 */
@Entity
public class HotSearch extends AizouBaseEntity {

    @Transient
    public static String FD_ID = "id";
    @Transient
    public static String FD_ITEMID = "itemId";
    @Transient
    public static String FD_SEARCHTYPE = "searchType";
    @Transient
    public static String FD_SEARCHFIELD = "searchField";
    @Transient
    public static String FD_SEARCHCONTENT = "searchContent";
    @Transient
    public static String SEARCH_TYPE_VS = "viewspot";

    @Transient
    public static String SEARCH_TYPE_HOTEL = "hotel";

    @Transient
    public static String SEARCH_TYPE_RESTAURANT = "restaurant";

    @Transient
    public static String SEARCH_TYPE_SHOPPING = "shopping";

    @Transient
    public static String SEARCH_TYPE_TRAVELNOTE = "travelNote";

    @Transient
    public static String SEARCH_TYPE_LOCALITY = "locality";

    @Transient
    public static String SEARCH_FIELD_LOCALITY = "locality";

    @Transient
    public static String SEARCH_FIELD_CONTENT = "content";

    @Transient
    public static String SEARCH_FIELD_STYLE = "style";

    private String searchType;

    private String searchField;

    private String itemName;

    private ObjectId itemId;

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchField() {
        return searchField;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }
}
