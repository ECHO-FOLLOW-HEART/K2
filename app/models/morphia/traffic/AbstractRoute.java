package models.morphia.traffic;

import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import play.data.validation.Constraints;

import java.util.Date;

/**
 * 交通信息的抽象类。
 *
 * @author Zephyre
 */
public abstract class AbstractRoute extends TravelPiBaseItem implements ITravelPiFormatter {
    @Constraints.Required
    @Embedded
    public SimpleRef depStop;

    @Constraints.Required
    @Embedded
    public SimpleRef arrStop;

    @Constraints.Required
    public String code;

    @Constraints.Required
    @Embedded
    public SimpleRef depLoc;

    @Constraints.Required
    @Embedded
    public SimpleRef arrLoc;

    public Integer distance;

    @Constraints.Required
    public Integer timeCost;

    @Constraints.Required
    public Date depTime;

    @Constraints.Required
    public Date arrTime;
}
