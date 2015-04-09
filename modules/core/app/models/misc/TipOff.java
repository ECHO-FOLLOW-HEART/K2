package models.misc;

import models.AizouBaseEntity;

/**
 * 举报
 * <p>
 * Created by topy on 2015/4/9.
 */
public class TipOff extends AizouBaseEntity {

    /**
     * 举报内容
     */
    public String body;

    /**
     * 举报对象
     */
    public Long targetUserId;

    /**
     * 提交举报的人
     */
    public Long offerUserId;

    /**
     * 创建时间
     */
    public Long cTime;

    /**
     * 创建时间
     */
    public String Date;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getOfferUserId() {
        return offerUserId;
    }

    public void setOfferUserId(Long offerUserId) {
        this.offerUserId = offerUserId;
    }

    public Long getcTime() {
        return cTime;
    }

    public void setcTime(Long cTime) {
        this.cTime = cTime;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
