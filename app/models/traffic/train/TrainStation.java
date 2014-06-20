package models.traffic.train;

import models.geos.Address;
import models.geos.Locality;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * 火车站。
 *
 * @author Zephyre
 */
@Entity
public class TrainStation extends Model{
    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Embedded
    public Address address;

    /**
     * 车站唯一代码，如：北京西：21152，北京南：10025等。
     */
    @Constraints.MaxLength(3)
    public String stationCode;

    /**
     * 所在城市。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Locality locality;
}
