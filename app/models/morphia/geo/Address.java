package models.morphia.geo;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Reference;

/**
 * 地址。
 *
 * @author Zephyre
 */
@Embedded
public class Address {
    public String address;

    @Reference
    public Locality loc;

    @Embedded
    public Coords coords;
}
