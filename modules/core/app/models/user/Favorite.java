package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 用户的收藏夹
 * <p/>
 * Created by topy on 2014/10/27.
 */
@Entity
@JsonFilter("favoriteFilter")
public class Favorite extends AizouBaseEntity {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnItemId = "itemId";


    @Transient
    public static String fnUserId = "userId";

    @Transient
    public static String fnType = "type";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnImage = "images";

    @Transient
    public static String fnLocality = "locality";

    @Transient
    public static String fnCreateTime = "createTime";

    @Transient
    public static String TYPE_VS = "vs";

    @Transient
    public static String TYPE_HOTEL = "hotel";

    @Transient
    public static String TYPE_RESTAURANT = "restaurant";

    @Transient
    public static String TYPE_SHOPPING = "shopping";

    @Transient
    public static String TYPE_ENTERTAINMENT = "entertainment";

    @Transient
    public static String TYPE_TRAVELNOTE = "travelNote";

    @Transient
    public static String TYPE_LOCALITY = "locality";

    @Transient
    public static String fnDesc = "desc";

    @Transient
    public static String fnTimeCostDesc = "timeCostDesc";

    @Transient
    public static String fnRating= "rating";

    @Transient
    public static String fnPriceDesc = "priceDesc";

    @Transient
    public static String fnAddress = "address";

    @Transient
    public static String fnTelephone = "telephone";


    /**
     * 用户ID
     */
    @NotNull
    public Integer userId;

    public ObjectId itemId;

    public String type;

    public String zhName;

    public String enName;

    public String desc;

    public List<ImageItem> images;

    public Date createTime;

    /**
     * 所在目的地
     */
    public Locality locality;
    /**
     * 目的地和景点显示建议游玩时间
     */
    public String timeCostDesc;
    /**
     * 酒店和美食显示价格
     */
    public String priceDesc;
    /**
     * 酒店、美食、购物显示评分
     */
    public Double rating;
    /**
     * 地址
     */
    public String address;
    /**
     * 电话
     */
    public String telephone;

}
