package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseItem;
import models.poi.AbstractPOI;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/11/4.
 */
@Embedded
@JsonFilter("itinerItemFilter")
public class ItinerItem extends AizouBaseItem {

    @Transient
    public static String fdDayIndex = "dayIndex";
    @Transient
    public static String fdType = "type";
    @Transient
    public static String fdPoi = "poi";

    public Integer dayIndex;

    public String type;

    @Embedded
    public AbstractPOI poi;
}
