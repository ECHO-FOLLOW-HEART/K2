package models.morphia.misc;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 去正规化的简要引用。
 *
 * @author Zephyre
 */
@Embedded
public class SimpleRef {
    public ObjectId id;
    public String enName;
    public String zhName;
}
