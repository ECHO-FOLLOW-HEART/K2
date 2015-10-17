package models.poi;

import models.misc.ImageItem;

import java.util.List;

/**
 * 景点信息。
 *
 * @author Zephyre
 */
public class Shopping extends AbstractPOI {

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }
}
