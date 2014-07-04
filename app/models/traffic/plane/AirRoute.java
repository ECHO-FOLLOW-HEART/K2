package models.traffic.plane;

import models.misc.Currency;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * 航班信息。
 *
 * @author Zephyre
 */
@Entity
public class AirRoute extends Model {
    public static Finder<String, AirRoute> finder = new Finder<>(String.class, AirRoute.class);

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
     * 飞机型号描述。
     */
    public String jetDescription;

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
     * 出发机场航站楼。
     */
    public String departureTerminal;

    /**
     * 到达机场。
     */
    @Constraints.Required
    @ManyToOne
    public Airport arrival;

    /**
     * 到达机场航站楼。
     */
    public String arrivalTerminal;

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
    public Integer dayLag = 0;

    /**
     * 全程耗时（以分钟为单位）。
     */
    public Integer duration;

    /**
     * 全程总路程（以千米为单位）。
     */
    public Integer distance;

    /**
     * 正点率。
     */
    public Float onTimeStat;

    /**
     * 是否提供免费航餐。
     */
    public boolean offerFood;

    /**
     * 是否支持自助值机。
     */
    public boolean selfCheckin;

    /**
     * 是否有经停。
     */
    public boolean nonStopFlight = true;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    public List<FlightPrice> priceList;

    /**
     * 价格所用货币。
     */
    public Currency currency;

    /**
     * 参考价格（取所有代理商报价的最低值）。
     */
    public Integer price;

    @Version
    public Timestamp updatedTime;
}
