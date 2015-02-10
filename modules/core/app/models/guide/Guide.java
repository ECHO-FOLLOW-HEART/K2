package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.mongodb.morphia.annotations.Embedded;
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

    @Transient
    public static final String fnDayCnt = "dayCnt";

    @Transient
    public static final String fnSummary = "summary";

    public Integer userId;

    public Integer itineraryDays;

    public long updateTime;

    @Embedded
    public Integer dayCnt;
    @Embedded
    public String summary;


}
