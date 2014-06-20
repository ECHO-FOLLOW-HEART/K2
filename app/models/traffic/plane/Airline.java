package models.traffic.plane;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * 航空公司。
 *
 * @author Zephyre
 */
@Entity
public class Airline extends Model{
    @Id
    public Long id;

    /**
     * 航空公司名称。
     */
    @Constraints.Required
    public String airlineName;

    /**
     * 航空公司代码。
     */
    @Constraints.Required
    public String airlineCode;

    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    public List<FlightSchedule> flightScheduleList;
}
