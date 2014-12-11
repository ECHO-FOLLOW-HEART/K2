package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 推薦
 * Created by lxf on 14-11-12.
 */
@JsonFilter("commentFilter")
@Entity
public class Comment extends AizouBaseEntity {

    @Transient
    public static final String FD_AVATAR = "avatar";

    @Transient
    public static final String FD_NICK_NAME = "nickName";

    @Transient
    public static final String FD_USER_ID = "userId";

    @Transient
    public static final String FD_RATING = "rating";

    @Transient
    public static final String FD_CONTENTS = "commentDetails";

    @Transient
    public static final String FD_TIME = "commentTime";

    @Transient
    public static final String FD_IMAGS = "images";

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 评论的类型
     */
    private String poiType;

    /**
     * 评分数
     */
    private Double rating;

    /**
     * 评价的详情
     */
    private String commentDetails;

    /**
     * 评价时间
     */
    private long commentTime;

    /**
     * 评价的poiId
     */
    private ObjectId itemId;

    /**
     * 评论附带的照片
     */
    private List<ImageItem> images;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPoiType() {
        return poiType;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCommentDetails() {
        return commentDetails;
    }

    public void setCommentDetails(String commentDetails) {
        this.commentDetails = commentDetails;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(long commentTime) {
        this.commentTime = commentTime;
    }

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }
}
