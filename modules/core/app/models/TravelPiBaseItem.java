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
    public ObjectId id;
    public boolean enabled;
    public Map<String, Object> misc;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
