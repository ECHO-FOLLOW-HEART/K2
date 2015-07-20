package models.geo;

import models.AizouBaseEntity;
import models.AizouBaseItem;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by topy on 2015/7/20.
 */
@Embedded
public class Continent extends AizouBaseEntity {

    String zhName;

    String enName;

    String code;

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
