package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Locality
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("destinationFilter")
public class Destination extends TravelPiBaseItem {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnTravelMonth = "travelMonth";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnAlias = "alias";

    @Transient
    public static String fnTimeCostDesc = "timeCostDesc";

    @Transient
    public static String fnDesc = "desc";

    @Transient
    public static String fnImage = "images";

    @Transient
    public static String fnLocalities = "localities";

    @Transient
    public static String fnCountry = "country";

    public String zhName;

    public String enName;

    public List<String> alias;

    public String timeCostDesc;

    public String travelMonth;

    public String desc;

    public Integer level;

    public List<ImageItem> images;

    public List<Locality> localities;

    public Country country;

    public List<String> tags;

    public boolean abroad;

}
