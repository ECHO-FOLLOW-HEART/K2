package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseItem;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * 符合GeoJson规范的坐标
 * <p/>
 * Created by zephyre on 11/11/14.
 */
@Embedded
@JsonFilter("geoJsonPointFilter")
public class GeoJsonPoint extends AizouBaseItem {

    @Transient
    public static String fnType = "type";

    @Transient
    public static String FD_COORDS = "coordinates";

    public static String type = "Point";

    private double[] coordinates;

    public GeoJsonPoint() {
    }

    private GeoJsonPoint(double lng, double lat) {
        coordinates = new double[]{lng, lat};
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }
}
