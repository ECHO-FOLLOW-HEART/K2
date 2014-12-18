package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 用户基本信息。
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("relationshipFilter")
public class Relationship extends AizouBaseEntity {

    @Transient
    public static String FD_UserA = "userA";

    @Transient
    public static String FD_UserB = "userA";

    private Long userA;

    private Long userB;

    public Long getUserA() {
        return userA;
    }

    public void setUserA(Long userA) {
        this.userA = userA;
    }

    public Long getUserB() {
        return userB;
    }

    public void setUserB(Long userB) {
        this.userB = userB;
    }
}
