package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import models.geo.Locality;
import models.poi.ViewSpot;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;
import scala.Int;
import utils.Constants;

import java.util.List;
import java.util.Map;

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
    public static String fnCovers = "covers";

    @Transient
    public static String fnNoteCovers = "noteCovers";

    @Transient
    public static String fnAuthorName = "authorName";

    @Transient
    public static String fnAuthorAvatar = "authorAvatar";

    @Transient
    public static String fnSource = "source";

    @Transient
    public static String fnSourceUrl = "sourceUrl";

    @Transient
    public static String fnPublishTime = "publishTime";
    @Transient
    public static String fnTravelTime = "travelTime";

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
    public static String fnLowerCost = "lowerCost";
    @Transient
    public static String fnCostUpper = "costUpper";
    @Transient
    public static String fnUpperCost = "upperCost";
    @Transient
    public static String fnFavorCnt = "favorCnt";

    @Transient
    public static String fnCommentCnt = "commentCnt";

    @Transient
    public static String fnViewCnt = "viewCnt";
    @Transient
    public static String fnRating = "rating";
    @Transient
    public static String fnEssence = "essence";

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

    public String authorName;
    /**
     * 作者头像
     */
    public String avatar;
    public String authorAvatar;

    /**
     * 作者的id
     */
    public Long authorId;

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
     * 分享次数
     */
    public Integer shareCnt;
    /**
     * 评分
     */
    public Double rating;
    /**
     * 热度
     */
    public Double hotness;

    /**
     * 游记中提到的景点
     */
    public List<ViewSpot> viewSpotList;

    /**
     * 游记中提到的目的地
     */
    public List<Locality> localityList;

    /**
     * 天数
     */
    public Integer lowerDays;

    /**
     * 天数
     */
    public Integer uppperDays;

    /**
     * 人均花销
     */
    public Double lowerCost;

    public Double upperCost;

    /**
     * 出游的月份/季节
     */
    public List<Integer> months;

    /**
     * 出游的时间
     */
    public Long travelTime;

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
    public List<Map<String, String>> contents;

    /**
     * 游记标签
     */
    public List<String> tags;
    /**
     * 游记正文
     */
    public String content;
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
    public Boolean essence;
    /**
     * 图像
     */
    public List<ImageItem> covers;
    public List noteCovers;

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