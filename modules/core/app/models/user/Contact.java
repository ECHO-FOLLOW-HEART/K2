package models.user;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;

import java.util.List;

/**
 * Created by topy on 2014/12/8.
 */
@JsonFilter("contactFilter")
public class Contact extends TravelPiBaseItem {

    public Integer entryId;

    public Integer sourceId;

    public String name;

    public String tel;

    public Boolean isUser;

    public Boolean isContact;

    public Integer userId;

}
