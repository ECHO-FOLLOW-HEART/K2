package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/9/13.
 */
@JsonFilter("recomFilter")
@Entity
public class Recom extends AizouBaseEntity {

    @Transient
    public static String fnId = "id";
    @Transient
    public static String fnTitle = "title";
    @Transient
    public static String fnDesc = "desc";
    @Transient
    public static String fnCover = "cover";
    @Transient
    public static String fnLinkType = "linkType";
    @Transient
    public static String fnLinkUrl = "linkUrl";
    @Transient
    public static String fnItemType = "itemType";
    @Transient
    public static String fnItemId = "itemId";


    /**
     * 推荐分类
     */
    public String type;

    /**
     * 推荐项目的类型
     */
    public String itemType;
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
    public String title;

    /**
     * 跳转类型:app-app内跳转，html-HTML5页面跳转
     */
    public String linkType;
    /**
     * 跳转页面
     */
    public String linkUrl;
    public Integer weight;

    public String itemId;

}
