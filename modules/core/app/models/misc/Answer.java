package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 问答
 * Created by lxf on 15-1-26.
 */
@Entity
@JsonFilter("qaFormatter")
public class Answer extends AizouBaseEntity implements ITravelPiFormatter {

    @Transient
    public static String fnTitle = "title";

    @Transient
    public static String fnAuthorName = "authorName";

    @Transient
    public static String fnAuthorAvatar = "authorAvatar";

    @Transient
    public static String fnContents = "contents";

    @Transient
    public static String fnPublishTime = "publishTime";

    @Transient
    public static String fnSource = "source";

    @Transient
    public static String fnEssence = "essence";

    /**
     * 标题
     */
    public String title;

    /**
     * 内容
     */
    public String contents;

    /**
     * 作者
     */
    public String authorName;

    /**
     * 头像
     */
    public String authorAvatar;

    /**
     * 发表时间
     */
    public long publishTime;

    /**
     * 标签
     */
    public List<String> tags;

    /**
     * 源source
     */
    public String source;

    /**
     * 精华答案
     */
    public Boolean essence;

    /**
     * 所属id
     */
    public String qId;

    @Override
    public JsonNode toJson() {
        return null;
    }
}
