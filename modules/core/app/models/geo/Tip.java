package models.geo;

import models.AizouBaseItem;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * 小贴士
 * <p/>
 * Created by topy on 2014/11/20.
 */
@Embedded
public class Tip extends AizouBaseItem {

    public String zhName;
    public String enName;
    public String desc;
    public List<ImageItem> images;


}
