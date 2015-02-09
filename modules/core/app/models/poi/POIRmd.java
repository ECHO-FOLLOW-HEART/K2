package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
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
public class POIRmd extends AizouBaseEntity {

    @Transient
    public static String fnTitle = "title";

    @Transient
    public static String fnImages = "images";

    @Transient
    public static String fnRating = "rating";

    /**
     * 推荐的poiId
     */
    private ObjectId poiId;

    /**
     * 推荐标题
     */
    private String title;

    /**
     * 推荐的图片
     */
    private List<ImageItem> images;

    /**
     * 评分
     */
    private Double rating;

    public ObjectId getPoiId() {
        return poiId;
    }

    public void setPoiId(ObjectId poiId) {
        this.poiId = poiId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
