package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * 模板路线。
 *
 * @author Zephyre
 */
@Entity
public class Plan extends AbstractPlan implements ITravelPiFormatter {

    @Transient
    public static final String FD_FORKED_CNT = "forkedCnt";

    @Transient
    public static final String FD_AUTHOR_AVATAR = "authorAvatar";

    @Transient
    public static final String FD_AUTHOR_NAME = "authorName";

    /**
     * 该路线被使用的次数。
     */
    private Integer forkedCnt;

    /**
     * 路线作者昵称
     */
    private String authorName;

    /**
     * 路线作者头像
     */
    private String authorAvatar;

    public Integer getForkedCnt() {
        return forkedCnt;
    }

    public void setForkedCnt(Integer forkedCnt) {
        this.forkedCnt = forkedCnt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    @Override
    public JsonNode toJson(boolean showDetails) {
        ObjectNode ret = (ObjectNode) super.toJson(showDetails);
        ret.put("forkedCnt", forkedCnt != null ? forkedCnt : 0);
        ret.put("authorName", authorName != null ? authorName : "");
        ret.put("authorAvatar", authorAvatar != null ? authorAvatar : "");
        return ret;
    }
}
