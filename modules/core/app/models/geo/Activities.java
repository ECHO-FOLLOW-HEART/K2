package models.geo;

import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * 活动
 *
 * Created by topy on 2014/11/20.
 */
@Embedded
public class Activities extends TravelPiBaseItem {

    public String zhName;
    public String enName;
    public String desc;
    public List<ImageItem> images;


}
