package models.user;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2015/8/27.
 */
public class ExpertInfo extends AizouBaseEntity {

    @Transient
    public static String fnUserId = "userId";
    @Transient
    public static String fnZone = "zone";
    @Transient
    public static String fnProfile = "profile";
    @Transient
    public static String fnTags = "tags";

    @Indexed
    private long userId;
    @Indexed
    private List<ObjectId> zone;
    @Indexed
    private List<ObjectId> travelNote;

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

    public List<ObjectId> getTravelNote() {
        return travelNote;
    }

    public void setTravelNote(List<ObjectId> travelNote) {
        this.travelNote = travelNote;
    }
}
