package models.backup.misc;

import models.backup.poi.Restaurant;
import play.data.validation.Constraints;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * 餐厅的图像。
 *
 * @author Zephyre
 */
//@Entity
public class RestaurantImage extends ImageItem {
    @ManyToOne(fetch = FetchType.LAZY)
    public Restaurant restaurant;

    /**
     * 图像的顺序。
     */
    @Constraints.Required
    public Integer priority = 0;
}
