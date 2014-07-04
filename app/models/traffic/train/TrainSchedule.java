package models.traffic.train;

import com.avaje.ebean.annotation.EnumValue;
import models.misc.Currency;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import java.sql.Time;

/**
 * 车次的详细时刻表。
 *
 * @author Zephyre
 */
@Entity
public class TrainSchedule extends Model {
    public static Finder<Long, TrainSchedule> finder = new Finder<>(Long.class, TrainSchedule.class);
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
    @Min(0)
    public Integer stopIdx;
    /**
     * 站点类型。1为始发站，2为终到站，3为普通中间站。
     */
    @Constraints.Required
    public StopType stopType;
    /**
     * 到达时间。如果该站点为始发站，则为NULL。
     */
    public Time arrivalTime;
    /**
     * 出发时间。如果该站点为终到站，则为NULL。
     */
    public Time departureTime;
    /**
     * 如果到达时间和发车时间不在同一天（跨越0点），dayLag可能为1.
     */
    @Constraints.Required
    @Min(0)
    public Integer dayLag = 0;
    /**
     * 列车运行时间（以分钟为单位）。
     */
    @Constraints.Required
    @Min(0)
    public Integer runTime = 0;
    /**
     * 运行里程（千米）
     */
    @Constraints.Required
    @Min(0)
    public Integer distance = 0;
    /**
     * 货币单位。
     */
    public Currency currency;
    /**
     * 基础票价。
     */
    public Integer price;

    /**
     * 车站的类型。
     */
    public enum StopType {
        /**
         * 始发站
         */
        @EnumValue("S")
        START,
        /**
         * 终到站
         */
        @EnumValue("E")
        END,
        /**
         * 中间站
         */
        @EnumValue("N")
        NORMAL
    }

}
