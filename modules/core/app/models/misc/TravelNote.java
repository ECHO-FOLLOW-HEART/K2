package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;
import utils.Constants;

import java.util.List;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("travelNoteFilter")
public class TravelNote extends AizouBaseEntity implements ITravelPiFormatter {

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
    public static String fnAuthorName = "author";

    @Transient
    public static String fnAuthorAvatar = "avatar";

    @Transient
    public static String fnSource = "source";

    @Transient
    public static String fnSourceUrl = "sourceUrl";

    @Transient
    public static String fnPublishDate = "publishTime";

    @Transient
    public static String fnStartDate = "startDate";

    @Transient
    public static String fnSummary = "summary";

    @Transient
    public static String fnContents = "contentsList";
    @Transient
    public static String fnNoteContents = "contents";
    @Transient
    public static String fnCostLower = "costLower";

    @Transient
    public static String fnCostUpper = "costUpper";

    @Transient
    public static String fnFavorCnt = "favorCnt";

    @Transient
    public static String fnCommentCnt = "commentCnt";

    @Transient
    public static String fnViewCnt = "viewCnt";

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
    public String author;

    /**
     * 作者头像
     */
    public String avatar;

    /**
     * 发表时间
     */
    public Long publishTime;

    /**
     * 发表时间
     */
    public String startDate;
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
    public List<String> contentsList;

    /**
     * 游记正文
     */
    public String contents;
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

/*    public String getPublishDate() {
        if (publishTime == null)
            return "";
        else
            return new SimpleDateFormat("yyyy-MM-dd EE z").format(publishTime);

    }*/

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("id", this.getId().toString()).add("title", title).add("author", author)
                .add("cover", cover);
        for (String k : new String[]{"source", "sourceUrl", "summary", "author", "avatar", "title"}) {
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

        builder.add("publishTime", publishTime);

        return Json.toJson(builder.get());
    }
}
