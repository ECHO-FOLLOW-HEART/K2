package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.Map;

/**
 * Created by zephyre on 10/25/14.
 */
@Entity
@JsonFilter("proxyFilter")
public class Proxy extends AizouBaseEntity {

    public static final String FD_SCHEME = "scheme";
    public static final String FD_HOST = "host";
    public static final String FD_PORT = "port";
    public static final String FD_USER = "user";
    public static final String FD_PASSWD = "passwd";
    public static final String FD_LATENCY = "latency";
    public static final String FD_VERIFIED = "verified";
    public static final String FD_DESC = "desc";
    public static final String FD_VERIFIED_TIME = "verifiedTime";

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
