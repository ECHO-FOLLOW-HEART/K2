package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import models.poi.Cuisine;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;
import java.util.Map;

/**
 * Locality
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("destinationFilter")
public class Destination extends TravelPiBaseItem {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnTravelMonth = "travelMonth";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnAlias = "alias";

    @Transient
    public static String fnTimeCostDesc = "timeCostDesc";

    @Transient
    public static String fnDesc = "desc";

    @Transient
    public static String fnImage = "images";

    @Transient
    public static String fnLocalities = "localities";

    @Transient
    public static String fnCountry = "country";

    public String zhName;

    public String enName;

    public List<String> alias;

    /**
     * 建议游玩天数
     */
    public Double timeCost;

    public String timeCostDesc;

    public String travelMonth;

    public String desc;

    public Integer level;

    public List<ImageItem> images;

    public List<Locality> localities;

    public Country country;

    public List<String> tags;

    public boolean abroad;

    /**
     * 评价
     */
    public Double rating;
    /**
     * 热门程度
     */
    public Double hotness;

    /**
     * 收藏数量
     */
    public Integer favorCnt;

    /**
     * 去过人数
     */
    public Integer visitCnt;

    /**
     * 评价数量
     */
    public Integer commentCnt;

    /**
     * 外部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<String> remoteTraffic;

    /**
     * 内部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<String> localTraffic;

    /**
     * 购物综述，HTML格式
     */
    public String shoppingIntro;

    /**
     * 特产
     */
    public List<Commodities> commodities;

    /**
     * 美食综述，HTML格式
     */
    public String dinningIntro;

    /**
     * 特色菜式
     */
    public List<Cuisine> cuisines;

    /**
     * 活动综述
     */
    public String activityIntro;

    /**
     * 活动
     */
    public List<Activities> activities;

    /**
     * 小贴士
     */
    public List<Tip> tips;

    /**
     * 其它信息
     */
    public Map<String, Object> miscInfo;


}
