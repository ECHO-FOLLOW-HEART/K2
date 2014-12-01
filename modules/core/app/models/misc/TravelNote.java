package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;
import utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("travelNoteFilter")
public class TravelNote extends TravelPiBaseItem implements ITravelPiFormatter {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnName = "name";

    @Transient
    public static String fnTitle = "title";

    @Transient
    public static String fnDesc = "desc";

    @Transient
    public static String fnCover = "cover";

    @Transient
    public static String fnAuthorName = "authorName";

    @Transient
    public static String fnAuthorAvatar = "authorAvatar";

    @Transient
    public static String fnSource = "source";

    @Transient
    public static String fnSourceUrl = "sourceUrl";

    @Transient
    public static String fnPublishDate = "publishDate";


    /**
     * 名称(与Title名称一致)
     */
    public String name;
    /**
     * 游记标题
     */
    public String title;

    /**
     * 作者名称
     */
    public String authorName;

    /**
     * 作者头像
     */
    public String authorAvatar;

    /**
     * 发表时间
     */
    public Date publishDate;

    /**
     * 收藏次数
     */
    public Integer favorCnt;

    /**
     * 评论次数
     */
    public Integer commentCnt;

    /**
     * 浏览次数
     */
    public Integer viewCnt;

    /**
     * 花费下限
     */
    public Float costLower;

    /**
     * 花费上限
     */
    public Float costUpper;

    /**
     * 旅行开支
     */
    public Float costNorm;

    /**
     * 旅行天数
     */
    public Integer days;

    /**
     * 出发地
     */
    public String fromLoc;

    /**
     * 目的地
     */
    public List<String> toLoc;

    /**
     * 游记摘要
     */
    public String summary;

    /**
     * 游记正文
     */
    public List<String> contents;

    /**
     * 游记来源
     */
    public String source;

    /**
     * 游记原始网址
     */
    public String sourceUrl;

    /**
     * 是否为精华游记
     */
    public Boolean elite;

    public String cover;

    public String getName() {
        if (title == null)
            return "";
        else
            return title;
    }

    public String getCover() {
        if (cover == null)
            return "";
        else
            return cover;
    }

    public String getDesc() {
        if (summary == null)
            return "";
        else
            return StringUtils.abbreviate(summary, Constants.ABBREVIATE_LEN);
    }

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("title", title).add("authorName", authorName);
        for (String k : new String[]{"source", "sourceUrl", "summary", "authorName", "authorAvatar", "title"}) {
            try {
                Object val = TravelNote.class.getField(k).get(this);
                builder.add(k, val != null ? val.toString() : "");
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }

        for (String k : new String[]{"favorCnt", "commentCnt", "viewCnt"}) {
            try {
                Object val = TravelNote.class.getField(k).get(this);
                if (val != null)
                    builder.add(k, val);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }

        builder.add("publishDate", publishDate == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(publishDate));

        return Json.toJson(builder.get());
    }
}
