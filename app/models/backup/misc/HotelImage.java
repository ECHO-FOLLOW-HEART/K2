package models.backup.misc;

import models.backup.poi.Hotel;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * 酒店的图像。
 *
 * @author Zephyre
 */
@Entity
public class HotelImage extends ImageItem {
    @ManyToOne(fetch = FetchType.LAZY)
    public Hotel hotel;

    /**
     * 图像的顺序。
     */
    @Constraints.Required
    public Integer orderIx = 0;
}
