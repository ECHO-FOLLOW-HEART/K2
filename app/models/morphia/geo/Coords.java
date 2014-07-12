package models.morphia.geo;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 坐标类。
 *
 * @author Zephyre
 */
@Embedded
public class Coords {
    public Double lat;
    public Double lng;
    public Double blat;
    public Double blng;
}
