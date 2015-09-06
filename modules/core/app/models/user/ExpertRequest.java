package models.user;

import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2015/8/27.
 */
public class ExpertRequest extends AizouBaseEntity {

    @Transient
    public static String fnUserId = "userId";
    @Transient
    public static String fnTel = "tel";

    private Long userId;

    private String tel;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
