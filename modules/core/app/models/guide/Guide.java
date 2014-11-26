package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.poi.Restaurant;
import models.poi.Shopping;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

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

    public Integer userId;

    public Integer itineraryDays;


}
