package models;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 坐标类。
 *
 * @author Zephyre
 */
@Embedded
public class Coords {
    public double lat;
    public double lng;
    public double blat;
    public double blng;
}
