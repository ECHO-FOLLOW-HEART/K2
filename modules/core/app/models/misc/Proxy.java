package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.Map;

/**
 * Created by zephyre on 10/25/14.
 */
@Entity
@JsonFilter("proxyFilter")
public class Proxy extends TravelPiBaseItem {
    @Constraints.Required
    public String scheme;

    @Constraints.Required
    public String host;

    @Constraints.Required
    public Integer port;

    public String user;

    public String passwd;

    @Constraints.Required
    public Map<String, Float> latency;

    @Constraints.Required
    public Map<String, Boolean> verified;

    @Constraints.Required
    public Date verifiedTime;

    public String desc;
}
