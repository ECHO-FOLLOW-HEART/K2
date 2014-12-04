package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;

/**
 * TravelPi基础数据基类。
 *
 * @author Zephyre
 */
@Entity
public abstract class TravelPiBaseItem {
    @Transient
    public static final String FD_ENABLED = "enabled";

    @Id
    private ObjectId id;

    private boolean enabled;

    private Map<String, Object> misc;

    public ObjectId getId() {
        return id;
    }

    public Map<String, Object> getMisc() {
        return misc;
    }

    public void setMisc(Map<String, Object> misc) {
        this.misc = misc;
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
