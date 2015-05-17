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
    public static String FD_UserB = "userB";


    @Transient
    public static String FD_MemoA = "memoA";


    @Transient
    public static String FD_MemoB = "memoB";

    private Long userA;

    private Long userB;

    private String memoA;

    private String memoB;

    public String getMemoA() {
        return memoA;
    }

    public void setMemoA(String memoA) {
        this.memoA = memoA;
    }

    public String getMemoB() {
        return memoB;
    }

    public void setMemoB(String memoB) {
        this.memoB = memoB;
    }

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
