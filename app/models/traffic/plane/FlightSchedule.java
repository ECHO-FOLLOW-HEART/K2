package models.traffic.plane;

import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * 航班信息。
 *
 * @author Zephyre
 */
@Entity
public class FlightSchedule extends Model{
    @Id
    @Constraints.MaxLength(10)
    public String flightCode;

    /**
     * 表示本记录不是独立的航班，正在共享sharing所指定的航班代码。
     */
    public String sharing;

    /**
     * 飞机型号。
     */
    public String jetType;

    /**
     * 航空公司。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Airline airline;

    /**
     * 起飞机场。
     */
    @Constraints.Required
    @ManyToOne
    public Airport departure;

    /**
     * 到达机场。
     */
    @Constraints.Required
    @ManyToOne
    public Airport arrival;

    /**
     * 起飞时间。
     */
    public Time departureTime;

    /**
     * 到达时间。
     */
    public Time arrivalTime;

    /**
     * arrivalTime指的是第几天的时间。比如：dayLag=0：arrivalTime指的是出发当日即到达。
     */
    @Constraints.Required
    public Integer dayLag;

    /**
     * 全程耗时（以分钟为单位）。
     */
    public Integer duration;

    /**
     * 全程总路程（以千米为单位）。
     */
    public Integer distance;

    @UpdatedTimestamp
    public Timestamp updatedTime;
}
