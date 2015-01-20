package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;

/**
 * TravelPi基础数据基类。
 *
 * @author Zephyre
 */
@Entity
public abstract class AizouBaseEntity extends AizouBaseItem {

    @Transient
    public static final String FD_ENABLED = "enabled";

    @Transient
    public static final String FD_ID = "id";

    @Transient
    public static final String FD_IS_FAVORITE = "isFavorite";

    @Transient
    public static final String FD_TAOZIENA = "taoziEna";

    @Id
    private ObjectId id;

    private boolean enabled;

    private boolean taoziEna;

    public boolean isTaoziEna() {
        return taoziEna;
    }

    public void setTaoziEna(boolean taoziEna) {
        this.taoziEna = taoziEna;
    }

    private Map<String, Object> misc;

    /**
     * 是否收藏
     */
    private Boolean isFavorite;

    public Boolean getIsFavorite() {
        if (isFavorite == null)
            return false;
        else
            return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite){
        this.isFavorite = isFavorite;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Map<String, Object> getMisc() {
        return misc;
    }

    public void setMisc(Map<String, Object> misc) {
        this.misc = misc;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
