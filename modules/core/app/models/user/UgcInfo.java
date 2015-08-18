package models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.TravelNote;
import org.bson.types.ObjectId;
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

    @Transient
    public static String fnLikeLocalities = "likeLocalities";

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
     * 用户游记
     */
    private List<TravelNote> travelNotes;

    /**
     * 喜欢的城市
     */
    private List<ObjectId> likeLocalities;


}