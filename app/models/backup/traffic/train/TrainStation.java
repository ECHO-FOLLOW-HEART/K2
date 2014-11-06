package models.backup.traffic.train;

import models.backup.geos.Address;
import models.backup.geos.Locality;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * 火车站。
 *
 * @author Zephyre
 */
@Entity
public class TrainStation extends Model {
    public static Finder<Long, TrainStation> finder = new Finder<>(Long.class, TrainStation.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Embedded
    public Address address;

    /**
     * 拼音缩写。
     */
    @Constraints.MaxLength(16)
    @Column(length = 16)
    public String shortPY;

    /**
     * 车站拼音。
     */
    @Constraints.MaxLength(64)
    @Column(length = 64)
    public String pinyin;

    /**
     * 车站唯一代码，如：北京西：21152，北京南：10025等。
     */
    @Constraints.MaxLength(3)
    public String stationCode;

    /**
     * 所在城市。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Locality locality;
}
