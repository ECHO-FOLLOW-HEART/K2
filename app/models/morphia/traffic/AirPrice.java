package models.morphia.traffic;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 机票的价格信息。
 *
 * @author Zephyre
 */
@Embedded
public class AirPrice {
    public double discount;

    public double price;

    public double tax;

    public double surcharge;

    public String provider;
}
