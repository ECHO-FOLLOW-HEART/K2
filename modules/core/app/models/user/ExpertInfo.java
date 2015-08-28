package models.user;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by topy on 2015/8/27.
 */
public class ExpertInfo extends AizouBaseEntity {

    private long userId;

    private List<ObjectId> zone;

    private String profile;

    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<ObjectId> getZone() {
        return zone;
    }

    public void setZone(List<ObjectId> zone) {
        this.zone = zone;
    }
}
