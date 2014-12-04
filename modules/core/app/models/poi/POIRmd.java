package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * POI的网友推荐
 * Created by topy on 2014/11/21.
 */
@JsonFilter("poiRmdFilter")
@Entity
public class POIRmd extends TravelPiBaseItem {

    @Transient
    public static String fnTitle = "title";


    @Transient
    public static String fnImages = "images";

    @Transient
    public static String fnRating = "rating";

    /**
     * 推荐的poiId
     */
    public ObjectId poiId;

    /**
     * 推荐标题
     */
    public String title;

    /**
     * 推荐的图片
     */
    public List<ImageItem> images;

    /**
     * 评分
     */
    public Double rating;


}
