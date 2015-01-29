package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 微信文章（web用）
 *
 * Created by topy on 2015/1/28.
 */
@JsonFilter("articleFilter")
public class Article extends AizouBaseEntity {

    @Transient
    public static final String FD_TITLE = "title";

    @Transient
    public static final String FD_DESC = "desc";

    @Transient
    public static final String FD_SOURCE = "source";

    @Transient
    public static final String FD_AUTHORNAME = "authorName";

    @Transient
    public static final String FD_CONTENT = "content";

    @Transient
    public static final String FD_IMAGES = "images";

    @Transient
    public static final String FD_PUBLISHTIME = "publishTime";

    private String title;

    private String desc;

    private String source;

    private String authorName;

    private String content;

    private List<ImageItem> images;

    private long publishTime;

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }
}
