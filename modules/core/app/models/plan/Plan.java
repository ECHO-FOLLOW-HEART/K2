package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Entity;

/**
 * 模板路线。
 *
 * @author Zephyre
 */
@Entity
public class Plan extends AbstractPlan implements ITravelPiFormatter {

    /**
     * 该路线被使用的次数。
     */
    public Integer forkedCnt;

    /**
     * 路线作者昵称
     */
    public String authorName;

    /**
     * 路线作者头像
     */
    public String authorAvatar;


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
