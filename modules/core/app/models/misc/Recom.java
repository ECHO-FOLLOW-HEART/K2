package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/9/13.
 */
@JsonFilter("recomFilter")
@Entity
public class Recom extends TravelPiBaseItem {

    @Transient
    public static String fnId = "id";
    @Transient
    public static String fnZhName = "zhName";
    @Transient
    public static String fnEnName = "enName";
    @Transient
    public static String fnDesc = "desc";
    @Transient
    public static String fnCover = "cover";
    @Transient
    public static String fnLinkType = "linkType";
    @Transient
    public static String fnLinkUrl = "linkUrl";
    /**
     * 推荐类型
     */
    public String title;
    /**
     * 图片
     */
    public String cover;
    /**
     * 推荐描述
     */
    public String desc;
    /**
     * 推荐名称
     */
    public String zhName;

    /**
     * 推荐名称
     */
    public String enName;
    /**
     * 跳转类型:1.去攻略 2.去H5页面
     */
    public int linkType;
    /**
     * 跳转页面
     */
    public String linkUrl;
    public Integer weight;

}
