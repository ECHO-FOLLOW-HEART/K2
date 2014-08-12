package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

/**
 * TravelPi基础数据基类。
 *
 * @author Zephyre
 */
public abstract class TravelPiBaseItem {
    @Id
    public ObjectId id;

    public boolean enabled;
}
