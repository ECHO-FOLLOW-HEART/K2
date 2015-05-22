package models.misc;

import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/10/14.
 */
@Entity
public class Sequence extends AizouBaseEntity {

    @Transient
    public static String USERID = "UserID";
    public String column;
    public Long count;

}
