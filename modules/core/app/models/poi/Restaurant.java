package models.poi;

import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;

/**
 * 餐厅。
 *
 * @author Zephyre
 */
@Entity
public class Restaurant extends AbstractPOI {
    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }
}
