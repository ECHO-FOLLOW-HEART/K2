package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;

/**
 * TravelPi基础数据基类。
 *
 * @author Zephyre
 */
public abstract class TravelPiBaseItem {
    @Id
    public ObjectId id;
    public boolean enabled;
    public Map<String, Object> misc;

    public String getId() {
        return id.toString();
    }
}
