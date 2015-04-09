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

    @Transient
    public static final String fnVisibility = "visibility";

    @Transient
    public static final String fnVisibilityPublic = "public";

    @Transient
    public static final String fnVisibilityPrivate = "private";

    @Transient
    public static final String fnStatus = "status";

    @Transient
    public static final String fnStatusTraveled = "traveled";

    @Transient
    public static final String fnStatusPlanned = "planned";

    private Integer userId;

    private Integer itineraryDays;

    private Long updateTime;

    private Integer dayCnt;

    private String summary;

    private String detailUrl;

    /**
     * 可见度：public-所有人可见，private-自己可见
     */
    private String visibility;

    /**
     * 状态：traveled-已走过的，planned-计划的
     */
    private String status;

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

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

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
