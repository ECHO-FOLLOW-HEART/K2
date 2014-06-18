package models.misc;

import models.poi.Restaurant;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * 餐厅的图像。
 *
 * @author Haizi
 */
@Entity
public class RestaurantImage extends ImageItem {
    @ManyToOne(fetch = FetchType.LAZY)
    public Restaurant restaurant;

    /**
     * 图像的顺序。
     */
    @Constraints.Required
    public Integer priority = 0;
}
