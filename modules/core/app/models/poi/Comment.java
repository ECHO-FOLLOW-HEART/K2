package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.user.UserInfo;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by lxf on 14-11-12.
 */
@JsonFilter("commentsFilter")
@Entity
public class Comment extends TravelPiBaseItem implements ITravelPiFormatter {

    @Transient
    public static String fnUserInfo="userInfo";
    @Transient
    public static String fnScore="score";
    @Transient
    public static String fnCommentDetails="commentDetails";
    @Transient
    public static String fnCommentTime="commentTime";
    /**
     * 评论的用户
     */
    @Embedded
    public UserInfo userInfo;

    /**
     * 评论的类型
     */
    public String poiType;

    /**
     * 评分数
     */
    public Double score;
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
    public String poiId;


    public String getCommentTime(){
        DateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return timeFormat.format(commentTime);
    }

    @Override
    public JsonNode toJson() {
        return null;
    }
}
