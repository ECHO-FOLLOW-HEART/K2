package models;

import com.fasterxml.jackson.annotation.JsonValue;
import org.bson.types.ObjectId;

/**
 * Created by zephyre on 12/5/14.
 */
public class AizouObjectId extends ObjectId {
    @JsonValue
    @Override
    public String toString() {
        return super.toString();
    }
}
