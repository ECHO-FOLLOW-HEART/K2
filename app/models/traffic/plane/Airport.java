package models.traffic.plane;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * 飞机场。
 *
 * @author Zephyre
 */
@Entity
public class Airport extends Model{
    @Id
    public Long id;

//    @Constraints.Required
//    public String name;
//
//    @Embedded
//    public Address address;
//
//    /**
//     * IATA机场代码。
//     */
//    @Constraints.MaxLength(3)
//    public String iataCode;
//
//    /**
//     * 所在城市。
//     */
//    @Constraints.Required
//    @ManyToOne(fetch = FetchType.LAZY)
//    public Locality locality;
//
//    @CreatedTimestamp
//    public Time createdTime;
//
//    @Version
//    public Time updatedTime;
}
