package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/11/4.
 */
@Embedded
@JsonFilter("demoItemFilter")
public class DemoItem extends AizouBaseEntity {

    @Transient
    public static String fdDayIndex = "dayIndex";
    @Transient
    public static String fdType = "type";
    @Transient
    public static String fdLocality = "locality";

    public Integer dayIndex;

    public String type;
    // 内容
    public String desc;

}
