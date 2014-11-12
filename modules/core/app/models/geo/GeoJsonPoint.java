package models.geo;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 符合GeoJson规范的坐标
 *
 * Created by zephyre on 11/11/14.
 */
@Embedded
public class GeoJsonPoint {
    public static String type = "Point";

    public double[] getCoordinates() {
        return coordinates;
    }

    private double[] coordinates;

    public GeoJsonPoint(){}
    private GeoJsonPoint(double lng, double lat) {
        coordinates = new double[]{lng, lat};
    }

    public static GeoJsonPoint newInstance(double lng, double lat) {
        return new GeoJsonPoint(lng, lat);
    }
}
