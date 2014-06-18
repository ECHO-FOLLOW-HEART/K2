package models.traffic.train;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Time;

/**
 * 车次的详细时刻表。
 *
 * @author Haizi
 */
@Entity
public class TrainSchedule extends Model{
    @Id
    public Long id;

    @Constraints.Required
    @ManyToOne
    public TrainRoute route;

    @Constraints.Required
    @ManyToOne
    public TrainStation stop;

    /**
     * 站点序号。
     */
    @Constraints.Required
    public Integer stopIdx;

    /**
     * 站点类型。1为始发站，2为终到站，3为普通中间站。
     */
    @Constraints.Required
    public Integer stopType;

    /**
     * 到达时间。如果该站点为始发站，则为NULL。
     */
    public Time arrivalTime;

    /**
     * 出发时间。如果该站点为终到站，则为NULL。
     */
    public Time departureTime;

}
