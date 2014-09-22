package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
     * 出发时间。
     */
    public Date startDate;
    /**
     * 返程时间。
     */
    public Date endDate;
    /**
     * 更新时间。
     */
    public long updateTime;

    /**
     * 表明该UGC路线是基于哪一条模板。
     */
    public ObjectId templateId;
    /**
     * 用户路线来源于Web。
     */
    public boolean isFromWeb;

    /**
     * 用于WEB用户，true：最后要保存的数据，false：中间态
     */
    public boolean persisted;

    public UgcPlan() {

    }

    public UgcPlan(Plan plan) throws NoSuchFieldException, IllegalAccessException {
        this.tranfToUgcPlan(plan);
    }

    public UgcPlan(Plan plan, String uid, String startD, String endD, String id, String title) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ParseException {
        this.tranfToUgcPlan(plan);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.startDate = format.parse(startD);
        this.endDate = format.parse(endD);
        this.id = new ObjectId(id);
        this.title = title;
        this.uid = new ObjectId(uid);
        this.updateTime = (new Date()).getTime();
        this.enabled = true;
    }

    private void tranfToUgcPlan(Plan plan) throws NoSuchFieldException, IllegalAccessException {
        String paraFiledStr = null;
        Field thisField = null;

        //获取传进来的映射对象
        Class c = plan.getClass();
        //取得对象的所有属性，放到一个数组中
        Field[] f = c.getFields();
        for (int i = 0; i < f.length; i++) {
            paraFiledStr = f[i].getName();
            thisField = this.getClass().getField(paraFiledStr);
            //获取对象的属性
            Object filedValue = Plan.class.getField(f[i].getName()).get(plan);
            thisField.setAccessible(true);
            thisField.set(this, filedValue);
        }
        //设置ID
        this.id = new ObjectId();
        this.templateId = plan.id;
        this.enabled = true;
    }

    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        //更新时间
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        if (!showDetails) {
            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
            builder.add("_id", id.toString());
            builder.add("title", (title != null && !title.isEmpty()) ? title : "");
            builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? Json.toJson(imageList) : new ArrayList<>());
            //全程天数取优化后天数
            builder.add("days", details.size());
            builder.add("updateTime", updateTime);
            builder.add("startDate", startDate != null ? fmt.format(startDate) : "");
            builder.add("endDate", endDate != null ? fmt.format(endDate) : "");

            return Json.toJson(builder.get());
        }
        ObjectNode node = (ObjectNode) super.toJson(showDetails);
        if (uid != null)
            node.put("uid", uid.toString());
        if (templateId != null)
            node.put("templateId", templateId.toString());
        if (startDate != null)
            node.put("startDate", fmt.format(startDate));
        if (endDate != null)
            node.put("endDate", fmt.format(endDate));
        node.put("updateTime", updateTime);

        return node;
    }
}
