package models.backup.poi;

import com.avaje.ebean.annotation.CreatedTimestamp;
import models.backup.geos.Address;
import models.backup.geos.Locality;
import models.backup.tag.POITag;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * POI的基类。
 *
 * @author Zephyre
 */
@MappedSuperclass
public class POI extends Model {
    @Id
    public Long id;

    /**
     * 英文名称。
     */
    public String enName;

    /**
     * 中文名称。
     */
    public String zhName;

    /**
     * 当地语言名称。
     */
    public String localName;

    /**
     * 地址。
     */
    @Embedded
    public Address addr;

    /**
     * 所在城市。
     */
    @Constraints.Required
    @ManyToOne(fetch = FetchType.LAZY)
    public Locality locality;

    /**
     * 标签。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public List<POITag> tagList;

    /**
     * 介绍。
     */
    @Column(columnDefinition = "TEXT")
    public String description;

    @CreatedTimestamp
    public Timestamp createdTime;

    @Version
    public Timestamp updatedTime;

}
