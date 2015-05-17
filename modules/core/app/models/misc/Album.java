package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 相册集合(废弃)
 *
 * @author Zephyre
 */
@Entity
public class Album extends AizouBaseEntity {

    @Transient
    public static final String FD_IMAGE = "image";

    @Transient
    public static final String FD_USERID = "userId";

    @Transient
    public static final String FD_CTIME = "cTime";


    /**
     * 照片信息
     */
    @Embedded
    private ImageItem image;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Long cTime;

    public Long getcTime() {
        return cTime;
    }

    public void setcTime(Long cTime) {
        this.cTime = cTime;
    }

    public ImageItem getImage() {
        return image;
    }

    public void setImage(ImageItem image) {
        this.image = image;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
