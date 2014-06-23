package models.traffic.plane;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * 航空公司。
 *
 * @author Zephyre
 */
@Entity
public class Airline extends Model {
    public static Finder<String, Airline> finder = new Finder<>(String.class, Airline.class);

    /**
     * 航空公司代码。
     */
    @Id
    @Constraints.MaxLength(3)
    @Column(length = 3)
    public String airlineCode;

    /**
     * 航空公司名称。
     */
    @Constraints.Required
    public String airlineName;

    /**
     * 航空公司全称。
     */
    public String airlineFullName;

    /**
     * 航空公司简称。
     */
    public String airlineShortName;

    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    public List<AirRoute> airRouteList;
}
