package models.backup.traffic.plane;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 机票代理商信息。
 *
 * @author Zephyre
 */
@Entity
public class AirTicketAgency extends Model {
    public static Finder<Long, AirTicketAgency> finder = new Finder<>(Long.class, AirTicketAgency.class);

    @Id
    public Long id;

    /**
     * 代理商的名称。
     */
    @Constraints.Required
    public String name;

    /**
     * 代理商的电话联系方式。
     */
    public String telephone;

    /**
     * 代理商的email联系方式。
     */
    @Constraints.Email
    public String email;
}
