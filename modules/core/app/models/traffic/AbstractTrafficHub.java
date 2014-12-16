package models.traffic;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import models.geo.Address;
import models.misc.Contact;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * Created by topy on 2014/12/11.
 */
@JsonFilter("abstractTrafficHubFilter")
public abstract class AbstractTrafficHub extends AizouBaseEntity implements ITravelPiFormatter {

    @Transient
    public static final String FD_ID = "id";

    @Transient
    public static final String FD_ZHNAME = "zhName";


    @Transient
    public static final String FD_ENNAME= "enName";

    @Transient
    public static final String FD_DESC = "desc";

    @Embedded
    public Address addr;

    public String zhName;

    public String enName;

    public String url;

    public String desc;

    @Embedded
    public Contact contact;

    public List<String> alias;
}
