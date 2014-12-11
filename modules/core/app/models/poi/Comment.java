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
    public static final String FD_AVATAR = "userAvatar";

    @Transient
    public static final String FD_ITEM_ID = "itemId";

    @Transient
    public static final String FD_USER_NAME = "userName";

    @Transient
    public static final String FD_USER_ID = "userId";

    @Transient
    public static final String FD_RATING = "rating";

    @Transient
    public static final String FD_CONTENTS = "contents";

    @Transient
    public static final String FD_CTIME = "cTime";

    @Transient
    public static final String FD_MTIME = "mTime";

    @Transient
    public static final String FD_IMAGES = "images";

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户昵称
     */
    private String userName;

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
    private String contents;

    /**
     * 评论发表时间
     */
    private long cTime;

    /**
     * 评论修改时间
     */
    private long mTime;

    /**
     * 评论对应的item
     */
    private ObjectId itemId;

    /**
     * 评论附带的照片
     */
    private List<ImageItem> images;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public long getcTime() {
        return cTime;
    }

    public void setcTime(long cTime) {
        this.cTime = cTime;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
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
