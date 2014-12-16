package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by lxf on 14-11-12.
 */
@JsonFilter("travelColumnsFilter")
@Entity
public class Column extends AizouBaseEntity {

    @Transient
    public static final String FD_COVER = "cover";

    @Transient
    public static final String FD_LINK = "link";
    @Transient
    public static final String FD_TITLE = "title";

    /**
     * 标题
     */
    private String title;

    /**
     * 图片的url
     */
    private String cover;

    /**
     * 跳转的url
     */
    private String link;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
