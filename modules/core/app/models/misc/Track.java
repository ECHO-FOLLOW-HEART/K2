package models.misc;

import models.AizouBaseEntity;
import models.geo.Country;
import models.geo.Locality;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexDirection;

/**
 * Created by topy on 2015/7/9.
 */
public class Track extends AizouBaseEntity {
    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnLocality = "locality";

    @Transient
    public static String fnCountry = "country";

    /**
     * 用户ID
     */
    private Long userId;
    @Indexed(value = IndexDirection.ASC, name = "itemId", unique = true, dropDups = true)
    private String itemId;

    private Locality locality;

    private Country country;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Locality getLocality() {
        return locality;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getItemId() {
        return this.itemId;
    }

    public String setItemId() {
        return this.itemId = userId.toString() + locality.getId().toString();
    }

}
