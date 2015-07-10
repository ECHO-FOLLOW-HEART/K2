package models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.TravelNote;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
public class UgcInfo extends AizouBaseEntity {

    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnTracks = "tracks";

    @Transient
    public static String fnTravelNotes = "travelNotes";

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * 用户类型
     */
    private List<String> roles;

    /**
     * 用户足迹
     */
    private List<Locality> tracks;

    /**
     * 用户游记
     */
    private List<TravelNote> travelNotes;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Locality> getTracks() {
        return tracks;
    }

    public void setTracks(List<Locality> tracks) {
        this.tracks = tracks;
    }

    public List<TravelNote> getTravelNotes() {
        return travelNotes;
    }

    public void setTravelNotes(List<TravelNote> travelNotes) {
        this.travelNotes = travelNotes;
    }
}