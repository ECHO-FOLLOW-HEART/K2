package models.morphia.user;

import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;

import java.util.Date;

/**
 * Created by topy on 2014/10/10.
 */
public class Credential extends TravelPiBaseItem {

    @Id
    public ObjectId id;

    @Constraints.Required
    public Integer userId;

    @Constraints.Required
    public String pwdHash;

    @Constraints.Required
    public String salt;

    public String testCode;






}
