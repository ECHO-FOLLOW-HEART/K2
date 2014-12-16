package models.misc;

import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by topy on 2014/10/14.
 */
@Entity
public class Sequence extends AizouBaseEntity {


    public static String USERID = "UserID";
    public String column;
    public Long count;

}
