package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import java.util.Date;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
public class UgcPlan extends Plan implements ITravelPiFormatter {

    /**
     * 用户ID
     */
    public ObjectId uid;
    /**
     * 表明该UGC路线是基于哪一条模板。
     */
    public ObjectId templateId;
    /**
     * 出发时间。
     */
    public Date startDate;
    /**
     * 返程时间。
     */
    public Date endDate;

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        ObjectNode node = (ObjectNode) super.toJson();
        if (uid != null)
            node.put("uid", uid.toString());
        if (templateId != null)
            node.put("templateId", templateId.toString());
        if (startDate != null)
            node.put("startDate", startDate.toString());
        if (endDate != null)
            node.put("endDate", endDate.toString());
        return node;
    }
}
