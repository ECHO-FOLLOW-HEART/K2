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
    public static final String FD_AVATAR = "authorAvatar";

    @Transient
    public static final String FD_ITEM_ID = "itemId";

    @Transient
    public static final String FD_AUTHOR_NAME = "authorName";

    @Transient
    public static final String FD_USER_ID = "userId";

    @Transient
    public static final String FD_RATING = "rating";

    @Transient
    public static final String FD_CONTENTS = "contents";

    @Transient
    public static final String FD_PUBLISHTIME = "publishTime";

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
    private String authorAvatar;


    /**
     * 用户昵称
     */
    private String authorName;

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
    private long publishTime;

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

    public String getAuthorAvatar() {
        if (authorAvatar == null || authorAvatar.equals("")) {
            return authorAvatar;
        }
        return String.format("http://images.taozilvxing.com/%s", authorAvatar);
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getAuthorName() {
        return authorName;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
