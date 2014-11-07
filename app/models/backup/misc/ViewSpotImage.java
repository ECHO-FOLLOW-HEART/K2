package models.backup.misc;

import models.backup.poi.ViewSpot;
import play.data.validation.Constraints;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * 景点的图像。
 *
 * @author Zephyre
 */
//@Entity
public class ViewSpotImage extends ImageItem {
    @ManyToOne(fetch = FetchType.LAZY)
    public ViewSpot viewSpot;

    /**
     * 图像的顺序。
     */
    @Constraints.Required
    public Integer priority = 0;
}