package models.backup.traffic.train;

import models.backup.misc.Currency;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Time;

/**
 * 列车车次信息。
 *
 * @author Zephyre
 */
@Entity
public class TrainRoute extends Model {
    public static Finder<String, TrainRoute> finder = new Finder<>(String.class, TrainRoute.class);

    /**
     * 车次。
     */
    @Id
    @Constraints.MaxLength(10)
    public String trainCode;

    /**
     * 车次别称。如K237/K240。
     */
    @Constraints.MaxLength(10)
    public String aliasCode;

    /**
     * 车次类别，如：高铁、动车、普快、特快等。
     */
    @Constraints.MaxLength(16)
    public String routeType;

    /**
     * 始发站。
     */
    @Constraints.Required
    public TrainStation departure;

    /**
     * 终到站。
     */
    @Constraints.Required
    public TrainStation arrival;

    /**
     * 出发时间。
     */
    @Constraints.Required
    public Time departureTime;

    /**
     * 到达时间。
     */
    @Constraints.Required
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

    /**
     * 票价的货币单位。
     */
    @ManyToOne
    public Currency currency;

    /**
     * 基本票价。同一车次，根据坐席等级不同，票价也不同。这里取最便宜的票价，作为基础价格，以供参考。以分为单位。
     */
    public Integer price;

    @Column(columnDefinition = "TEXT")
    public String memo;
}
