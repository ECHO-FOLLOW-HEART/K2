package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 首页专栏
 *
 * Created by lxf on 14-11-12.
 */
@JsonFilter("columnFilter")
@Entity
public class Column extends AizouBaseEntity {

    @Transient
    public static final String FD_COVER = "cover";

    @Transient
    public static final String FD_LINK = "link";

    @Transient
    public static final String FD_TITLE = "title";

    @Transient
    public static final String FD_CONTENT = "content";

    @Transient
    public static final String FD_TYPE = "type";


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

    /**
     * Html页的内容
     *
     * @return
     */
    private String content;

    /**
     * 显示类型：homepage-显示在首页的内容，recommend-显示在推荐页的内容
     */
    private String type;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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
