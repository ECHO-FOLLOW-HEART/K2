package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 攻略
 * Created by topy on 2014/11/4.
 */
@Entity
@JsonFilter("guideFilter")
public class Guide extends AbstractGuide {
    @Transient
    public static final String fnUserId = "userId";

    @Transient
    public static final String fnItineraryDays = "itineraryDays";

    @Transient
    public static final String fnUpdateTime = "updateTime";

    @Transient
    public static final String fnDayCnt = "dayCnt";

    @Transient
    public static final String fnSummary = "summary";

    private Integer userId;

    private Integer itineraryDays;

    private Long updateTime;

    private Integer dayCnt;

    private String summary;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItineraryDays() {
        return itineraryDays;
    }

    public void setItineraryDays(Integer itineraryDays) {
        this.itineraryDays = itineraryDays;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDayCnt() {
        return dayCnt;
    }

    public void setDayCnt(Integer dayCnt) {
        this.dayCnt = dayCnt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
