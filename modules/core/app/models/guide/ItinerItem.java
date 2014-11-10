package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import models.TravelPiBaseItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/11/4.
 */
@Embedded
@JsonFilter("itinerItemFilter")
public class ItinerItem extends TravelPiBaseItem {

    @Transient
    public static String fdDayIndex = "dayIndex";
    @Transient
    public static String fdType = "type";
    @Transient
    public static String fdPoi = "poi";

    public Integer dayIndex;

    public String type;

    @Embedded
    public ViewSpot viewSpot;


    @Embedded
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ViewSpot.class, name = "vs")})
    public AbstractPOI poi;
}
