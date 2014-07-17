package models.morphia.geo;


import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 地址。
 *
 * @author Zephyre
 */
@Embedded
public class Address {
    public String address;

    @Embedded
    public SimpleRef loc;

    @Embedded
    public Coords coords;
}
