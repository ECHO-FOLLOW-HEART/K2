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

    public Integer entryId;

    public Integer sourceId;

    public String name;

    public String tel;

    public Boolean isUser;

    public Boolean isContact;

    public Long userId;

    public String weixin;

}
