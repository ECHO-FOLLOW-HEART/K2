package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * Created by zephyre on 12/19/14.
 */
@Embedded
@JsonFilter("detailsEntryFilter")
public class DetailsEntry extends AizouBaseEntity {
    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String desc;

    /**
     * 图集
     */
    private List<ImageItem> images;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }
}
