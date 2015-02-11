package models.poi;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;

/**
 * Created by topy on 2015/2/11.
 */
public class LyMapping extends AizouBaseEntity {

    private ObjectId itemId;
    private String zhNameLxp;
    private String zhNameLy;
    private String lyCityId;
    private String locationName;
    private Integer lyId;

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public String getZhNameLxp() {
        return zhNameLxp;
    }

    public void setZhNameLxp(String zhNameLxp) {
        this.zhNameLxp = zhNameLxp;
    }

    public String getZhNameLy() {
        return zhNameLy;
    }

    public void setZhNameLy(String zhNameLy) {
        this.zhNameLy = zhNameLy;
    }

    public String getLyCityId() {
        return lyCityId;
    }

    public void setLyCityId(String lyCityId) {
        this.lyCityId = lyCityId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getLyId() {
        return lyId;
    }

    public void setLyId(Integer lyId) {
        this.lyId = lyId;
    }
}
