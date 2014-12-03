package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import exception.ErrorCode;
import exception.TravelPiException;
import models.ITravelPiFormatter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private ObjectId uid;

    /**
     * 出发时间。
     */
    private Date startDate;

    /**
     * 返程时间。
     */
    private Date endDate;

    /**
     * 更新时间。
     */
    private Long updateTime;

    /**
     * 表明该UGC路线是基于哪一条模板。
     */
    private ObjectId templateId;

    /**
     * 用户路线来源于Web。
     */
    private Boolean isFromWeb;

    /**
     * 用于WEB用户，true：最后要保存的数据，false：中间态
     */
    private Boolean persisted;

    public ObjectId getUid() {
        return uid;
    }

    public void setUid(ObjectId uid) {
        this.uid = uid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public ObjectId getTemplateId() {
        return templateId;
    }

    public void setTemplateId(ObjectId templateId) {
        this.templateId = templateId;
    }

    public Boolean getIsFromWeb() {
        return isFromWeb;
    }

    public void setIsFromWeb(Boolean isFromWeb) {
        this.isFromWeb = isFromWeb;
    }

    public Boolean getPersisted() {
        return persisted;
    }

    public void setPersisted(Boolean persisted) {
        this.persisted = persisted;
    }

    public UgcPlan() {
        this.updateTime = 0L;
        this.isFromWeb = false;
        this.persisted = false;
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterTypes().length == 0
                && !void.class.equals(method.getReturnType());
    }

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && void.class.equals(method.getReturnType());
    }

    public UgcPlan(Plan plan) throws TravelPiException {
        this();
        Class<?> cls = Plan.class;
        while (!cls.equals(Object.class)) {
            for (Method method : cls.getDeclaredMethods()) {
                if (!(Modifier.isPublic(method.getModifiers()) && isGetter(method)))
                    continue;

                String setterName = method.getName().replaceFirst("^get", "set");
                try {
                    Method setterMethod = this.getClass().getMethod(setterName, method.getReturnType());
                    if (!isSetter(setterMethod))
                        continue;
                    setterMethod.invoke(this, method.invoke(plan));
                } catch (ReflectiveOperationException ignored) {
                }
            }
            cls = cls.getSuperclass();
        }

        //设置ID
        this.setId(new ObjectId());
        this.templateId = plan.getId();
        this.setEnabled(true);
    }

    public UgcPlan(Plan plan, String uid, String startD, String endD, String id, String title) throws TravelPiException {
        this(plan);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            this.startDate = format.parse(startD);
            this.endDate = format.parse(endD);
        } catch (ParseException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, e.getMessage(), e);
        }

        this.setId(new ObjectId(id));
        this.setTitle(title);
        this.uid = new ObjectId(uid);
        this.updateTime = (new Date()).getTime();
        this.setEnabled(true);
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
            builder.add("_id", getId().toString());
            builder.add("title", (getTitle() != null && !getTitle().isEmpty()) ? getTitle() : "");
            builder.add("imageList", (getImageList() != null && !getImageList().isEmpty()) ?
                    Json.toJson(getImageList()) : new ArrayList<>());
            //全程天数取优化后天数
            builder.add("days", getDetails().size());
            builder.add("updateTime", updateTime);
            builder.add("startDate", startDate != null ? fmt.format(startDate) : "");
            builder.add("endDate", endDate != null ? fmt.format(endDate) : "");

            return Json.toJson(builder.get());
        }

        ObjectNode node = (ObjectNode) super.toJson(true);
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
