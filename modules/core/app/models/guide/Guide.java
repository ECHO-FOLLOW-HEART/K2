package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 攻略
 * Created by topy on 2014/11/4.
 */
@Entity
@JsonFilter("guideFilter")
public class Guide extends AbstractGuide {


    @Transient
    public static final String fnUserId = "userId";

    @Transient
    public static final String fnItineraryDays = "itineraryDays";

    @Transient
    public static final String fnUpdateTime = "updateTime";

    public Integer userId;

    public Integer itineraryDays;

    public long updateTime;


}
