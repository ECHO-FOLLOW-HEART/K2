package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by topy on 2014/12/8.
 */
@JsonFilter("contactFilter")
@Entity
public class Contact extends AizouBaseEntity {

    public int entryId;

    public int sourceId;

    public String name;

    public String tel;

    public boolean isUser;

    public boolean isContact;

    public long userId;

    public String weixin;

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public boolean isContact() {
        return isContact;
    }

    public void setContact(boolean isContact) {
        this.isContact = isContact;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }
}
