package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * 推薦
 * Created by lxf on 14-11-12.
 */
@JsonFilter("commentsFilter")
@Entity
public class Comment extends TravelPiBaseItem {

    @Transient
    public static String fnAvatar = "avatar";
    @Transient
    public static String fnNickName = "nickName";
    @Transient
    public static String fnScore = "rating";
    @Transient
    public static String fnCommentDetails = "commentDetails";
    @Transient
    public static String fnCommentTime = "commentTime";

    /**
     * 用户ID
     */
    public Integer userId;

    /**
     * 用户头像
     */
    public String avatar;

    /**
     * 用户昵称
     */
    public String nickName;

    /**
     * 评论的类型
     */
    public String poiType;

    /**
     * 评分数
     */
    public Double rating;
    /**
     * 评价的详情
     */
    public String commentDetails;
    /**
     * 评价时间
     */
    public long commentTime;

    /**
     * 评价的poiId
     */
    public ObjectId poiId;


    public String getCommentTime() {
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return timeFormat.format(commentTime);
    }

}
