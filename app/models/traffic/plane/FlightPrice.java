package models.traffic.plane;

import com.avaje.ebean.annotation.UpdatedTimestamp;
import models.misc.Currency;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 航班报价
 *
 * @author Zephyre
 */
@Entity
public class FlightPrice extends Model {
    @Id
    public Long id;

    /**
     * 航班信息。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public AirRoute route;

    /**
     * 机票代理商。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public AirTicketAgency agency;

    /**
     * 货币。默认为人民币。
     */
    @Constraints.Required
    public Currency currency;

    /**
     * 票价。
     */
    public Integer ticketPrice;

    /**
     * 票价折扣。
     */
    public Float discount;

    /**
     * 燃油附加费。
     */
    public Integer fuelSurcharge;

    /**
     * 其它税费（包括机场建设费）。
     */
    public Integer tax;

    /**
     * 舱位类型。参考IATA travel code规范。
     */
    public String travelClass;

    @Version
    public Timestamp updatedTime;
}
