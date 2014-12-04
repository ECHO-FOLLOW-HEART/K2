package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * 符合GeoJson规范的坐标
 * <p/>
 * Created by zephyre on 11/11/14.
 */
@Embedded
@JsonFilter("geoJsonPointFilter")
public class GeoJsonPoint extends TravelPiBaseItem {

    @Transient
    public static String fnType = "type";

    @Transient
    public static String fnCoordinates = "coordinates";

    public static String type = "Point";

    public double[] getCoordinates() {
        return coordinates;
    }

    private double[] coordinates;

    public GeoJsonPoint() {
    }

    private GeoJsonPoint(double lng, double lat) {
        coordinates = new double[]{lng, lat};
    }

    public static GeoJsonPoint newInstance(double lng, double lat) {
        return new GeoJsonPoint(lng, lat);
    }
}
