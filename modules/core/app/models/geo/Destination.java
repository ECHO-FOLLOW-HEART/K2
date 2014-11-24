package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

/**
 * 目的地类型
 *
 * Created by zephyre on 11/21/14.
 */
@Entity
@JsonFilter("destinationFilter")
public class Destination extends TravelPiBaseItem {

    public static String fnZhName = "zhName";

    public static String fnEnName = "enName";

    public static String fnAlias = "alias";

    public static String fnVisitCnt = "visitCnt";

    public static String fnCommentCnt = "commentCnt";

    public static String fnFavorCnt = "favorCnt";

    public static String fnRating = "rating";

    public static String fnAbroad = "abroad";

    public static String fnHotness = "hotness";

    public static String fnLocation = "location";

    public static String fnCountry = "country";

    public static String fnLocList = "locList";

    public static String fnTags = "tags";

    public static String fnImages = "images";

    public static String fnDesc = "desc";

    public static String fnTravelMonth = "travelMonth";

    public static String fnTimeCostDesc = "fnTimeCostDesc";

    /**
     * 中文名称
     */
    private String zhName;

    /**
     * 英文名称
     */
    private String enName;

    /**
     * 当地名称
     */
    private String locName;

    /**
     * 别名
     */
    private List<String> alias;

    /**
     * 去过的人数
     */
    private Integer visitCnt;

    /**
     * 评论条数
     */
    private Integer commentCnt;

    /**
     * 收藏次数
     */
    private Integer favorCnt;

    /**
     * 热门程度
     */
    private Double hotness;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 是否为境外目的地
     */
    private Boolean abroad;

    /**
     * 经纬度信息
     */
    private GeoJsonPoint location;

    /**
     * 所在国家（有效字段为_id, zhName, enName和code）
     */
    @Embedded
    private Country country;

    /**
     * 行政区从属链
     */
    private List<Destination> locList;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 照片
     */
    private List<ImageItem> images;

    /**
     * 简介
     */
    private String desc;

    /**
     * 最佳旅行时间
     */
    private String travelMonth;

    /**
     * 建议游玩时间
     */
    private String timeCostDesc;

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public Boolean getAbroad() {
        return abroad;
    }

    public void setAbroad(Boolean abroad) {
        this.abroad = abroad;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }

    public Integer getVisitCnt() {
        return visitCnt;
    }

    public void setVisitCnt(Integer visitCnt) {
        this.visitCnt = visitCnt;
    }

    public Integer getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(Integer commentCnt) {
        this.commentCnt = commentCnt;
    }

    public Integer getFavorCnt() {
        return favorCnt;
    }

    public void setFavorCnt(Integer favorCnt) {
        this.favorCnt = favorCnt;
    }

    public Double getHotness() {
        return hotness;
    }

    public void setHotness(Double hotness) {
        this.hotness = hotness;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Destination> getLocList() {
        return locList;
    }

    public void setLocList(List<Destination> locList) {
        this.locList = locList;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTravelMonth() {
        return travelMonth;
    }

    public void setTravelMonth(String travelMonth) {
        this.travelMonth = travelMonth;
    }

    public String getTimeCostDesc() {
        return timeCostDesc;
    }

    public void setTimeCostDesc(String timeCostDesc) {
        this.timeCostDesc = timeCostDesc;
    }

    /**
     * 建议游玩时间
     */

    private Integer timeCost;
}
