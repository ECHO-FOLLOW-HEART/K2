package models.geo;

import models.AizouBaseItem;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * 活动
 * <p/>
 * Created by topy on 2014/11/20.
 */
@Embedded
public class Activities extends AizouBaseItem {

    private String desc;

    private String title;

    private List<ImageItem> images;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
}
