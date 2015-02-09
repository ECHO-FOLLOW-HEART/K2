package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 目的地类型
 * <p>
 * Created by zephyre on 11/21/14.
 */
@Entity
@JsonFilter("localityFilter")
public class Locality extends AizouBaseEntity {

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_EN_NAME = "enName";

    @Transient
    public static final String FD_ALIAS = "alias";
    @Transient
    public static final String FD_LOCLIST = "locList";
    @Transient
    public static String fnVisitCnt = "visitCnt";
    @Transient
    public static String fnCommentCnt = "commentCnt";
    @Transient
    public static String fnFavorCnt = "favorCnt";
    @Transient
    public static String fnAbroad = "abroad";
    @Transient
    public static String fnCountry = "country";
    @Transient
    public static String fnTags = "tags";

    @Transient
    public static String fnImages = "images";

    @Transient
    public static String fnDesc = "desc";

    public static String fnLocation = "location";

    @Transient
    public static String fnHotness = "hotness";

    @Transient
    public static String fnRating = "rating";

    @Transient
    public static String fnTimeCost = "timeCost";

    @Transient
    public static String fnTimeCostDesc = "timeCostDesc";

    @Transient
    public static String fnTravelMonth = "travelMonth";

    @Transient
    public static String fnSuperAdm = "superAdm";

    public static String fnImageCnt = "imageCnt";

    @Transient
    public static String fnRemoteTraffic = "remoteTraffic";

    @Transient
    public static String fnLocalTraffic = "localTraffic";

    @Transient
    public static String fnShoppingIntro = "shoppingIntro";


    @Transient
    public static String fnDinningIntro = "diningIntro";

    @Transient
    public static String fnActivityIntro = "activityIntro";

    @Transient
    public static String fnActivities = "activities";

    @Transient
    public static String fnTips = "tips";

    @Transient
    public static String fnCommodities = "commodities";

    @Transient
    public static String fnCuisines = "cuisines";

    @Transient
    public static String fnSpecials = "specials";

    @Transient
    public static String fnGeoHistory = "geoHistory";

    /**
     * 外部交通信息。每个entry都是一个tip，为HTML格式
     */
    private List<DetailsEntry> remoteTraffic;

    /**
     * 内部交通信息。每个entry都是一个tip，为HTML格式
     */
    private List<DetailsEntry> localTraffic;

    /**
     * 购物综述，HTML格式
     */
    private String shoppingIntro;

    /**
     * 特产
     */
    private List<DetailsEntry> commodities;

    /**
     * 美食综述，HTML格式
     */
    private String diningIntro;

    /**
     * 特色菜式
     */
    private List<DetailsEntry> cuisines;

    /**
     * 活动综述
     */
    private String activityIntro;

    /**
     * 活动
     */
    private List<DetailsEntry> activities;

    /**
     * 小贴士
     */
    private List<DetailsEntry> tips;

    /**
     * 历史文化
     */
    private List<DetailsEntry> geoHistory;

    /**
     * 城市亮点
     */
    private List<DetailsEntry> specials;

    /**
     * 是否为热门城市
     */
    public Boolean isHot;
    public List<String> pinyin;
    public List<String> imageList;
    public boolean provCap;
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
     * 其它信息
     */
    //public Map<String, Object> miscInfo;

    /*
      可能废弃的字段-Start
     */
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
    @Embedded
    private GeoJsonPoint location;
    /**
     * 所在国家（有效字段为_id, zhName, enName和code）
     */
    @Embedded
    private Country country;
    /**
     * 行政区从属链
     */
    private List<Locality> locList;
    /**
     * 父行政区
     */
    private Locality superAdm;
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

    /**
     * 建议游玩时间
     */
    private Integer timeCost;

    public Locality getSuperAdm() {
        return superAdm;
    }

    public void setSuperAdm(Locality superAdm) {
        this.superAdm = superAdm;
    }

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
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

    public List<Locality> getLocList() {
        return locList;
    }

    public void setLocList(List<Locality> locList) {
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

    public List<DetailsEntry> getRemoteTraffic() {
        return remoteTraffic;
    }

    public List<DetailsEntry> getLocalTraffic() {
        return localTraffic;
    }

    public String getShoppingIntro() {
        return shoppingIntro;
    }

    public List<DetailsEntry> getCommodities() {
        return commodities;
    }

    public List<DetailsEntry> getCuisines() {
        return cuisines;
    }

    public String getActivityIntro() {
        return activityIntro;
    }

    public List<DetailsEntry> getActivities() {
        return activities;
    }

    public List<DetailsEntry> getTips() {
        return tips;
    }

    public List<DetailsEntry> getGeoHistory() {
        return geoHistory;
    }

    public List<DetailsEntry> getSpecials() {
        return specials;
    }

    public String getDiningIntro() {
        return diningIntro;
    }

    public void setDiningIntro(String diningIntro) {
        this.diningIntro = diningIntro;
    }
}