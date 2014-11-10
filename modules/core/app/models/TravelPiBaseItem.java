package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Map;

/**
 * TravelPi基础数据基类。
 *
 * @author Zephyre
 */
@Entity
public abstract class TravelPiBaseItem {
    @Id
    public String id;
    public boolean enabled;
    public Map<String, Object> misc;

    public String getId() {
        return id.toString();
    }
}
