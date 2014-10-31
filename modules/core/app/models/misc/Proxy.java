package models.misc;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

import java.util.Date;

/**
 * Created by zephyre on 10/25/14.
 */
@Entity
public class Proxy {
    @Id
    public ObjectId id;

    @Constraints.Required
    public String host;

    @Constraints.Required
    public Integer port;

    @Constraints.Required
    public Float latency;

    @Constraints.Required
    public Boolean verified;

    @Constraints.Required
    public Date verifiedTime;

    public String desc;
}
