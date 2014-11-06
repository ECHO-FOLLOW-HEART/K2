package models.backup.tag;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * POI的标签。
 *
 * @author Zephyre
 */
@Entity
public class POITag extends Model {
    @Id
    public Long id;

    public String tagName;

//    @ManyToMany(mappedBy = "tagList")
//    public List<POI> poiList;
}
