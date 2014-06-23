package models.traffic.plane;

import models.geos.Address;
import models.geos.Locality;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * 飞机场。
 *
 * @author Zephyre
 */
@Entity
public class Airport extends Model {
    public static Finder<Long, Airport> finder = new Finder<>(Long.class, Airport.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Embedded
    public Address address;

    /**
     * IATA机场代码。
     */
    @Constraints.MaxLength(3)
    public String iataCode;

    /**
     * 所在城市。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Locality locality;
}
