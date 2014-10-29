package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.geo.Address;
import models.misc.CheckinRatings;
import models.misc.Contact;
import models.misc.Description;
import models.misc.ImageItem;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;
import utils.Constants;
import utils.DataFilter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * POI的基类
 *
 * @author Zephyre
 *         Created by zephyre on 7/16/14.
 */
@JsonFilter("abstractPOIFilter")
public abstract class AbstractPOI extends TravelPiBaseItem implements ITravelPiFormatter {

    @Transient
    public static String simpID = "id";

    @Transient
    public static String simpName = "name";

    @Embedded
    public CheckinRatings ratings;

    @Embedded
    public Contact contact;

    @Embedded
    public Address addr;

    public String name;

    public String url;

    public Double price;

    public String priceDesc;

    public String desc;

    /**
     * 开放时间描述
     */
    public String openTime;

    public Integer openHour;

    public Integer closeHour;

    @Embedded
    public Description description;

    public String trafficInfo;

    public List<String> imageList;

    public List<ImageItem> images;

    public List<String> tags;

    public List<String> alias;

    /**
     * POI所在的行政区划。
     */
    public List<ObjectId> targets;

    /**
     * 表示该POI的来源。注意：一个POI可以有多个来源。
     * 示例：
     * <p>
     * source: { "baidu": {"url": "foobar", "id": 27384}}
     */
    public Map<String, Object> source;

    /**
     * 相关路线的统计数目。
     */
    public Integer relPlanCnt;

    /**
     * 其它信息
     */
    public Map<String, Object> extra;

    public static List<String> getRetrievedFields(int level) {
        switch (level) {
            case 1:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings"));
            case 2:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings", "desc", "imageList", "images", "tags"));
            case 3:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings", "desc", "imageList", "images", "tags", "contact", "url",
                        "price", "priceDesc", "alias"));
        }
        return new ArrayList<>();
    }

    public JsonNode toJson(int level) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // level1
        builder.add("_id", id.toString());
        if (ratings == null)
            ratings = new CheckinRatings();
        builder.add("ratings", ratings.toJson());
        builder.add("name", (name != null ? name : ""));

        // level2
        if (level > 1) {
            for (String k : new String[]{"imageList", "tags"}) {
                Field field;
                Object val = null;
                try {
                    field = AbstractPOI.class.getField(k);
                    val = field.get(this);
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
                boolean isNull = (val == null);
                if (val != null && val instanceof Collection)
                    isNull = ((Collection) val).isEmpty();
                builder.add(k, (isNull ? new ArrayList<>() : val));
            }

            if (images != null) {
                ArrayList<String> tmpList = new ArrayList<String>();
                for (ImageItem img : images.subList(0, (images.size() >= 5 ? 5 : images.size())))
                    tmpList.add(img.url);
                builder.add("imageList", tmpList);
            }

            // TODO 暂时兼容两种数据
            if (null != description) {
                builder.add("desc", (description.desc != null ? StringUtils.abbreviate(description.desc, Constants.ABBREVIATE_LEN) : desc != null ? desc : ""));
            } else {
                builder.add("desc", (desc != null ? StringUtils.abbreviate(desc, Constants.ABBREVIATE_LEN) : ""));
            }
            if (price != null)
                builder.add("price", price);
            builder.add("contact", (contact != null ? contact.toJson() : new HashMap<>()));

            // 如果存在更高阶的images字段，则使用之
            if (images != null && !images.isEmpty()) {
                List<ImageItem> imgList = new ArrayList<>();
                for (ImageItem img : images) {
                    if (img.enabled != null && !img.enabled)
                        continue;
                    imgList.add(img);
                }

                Collections.sort(imgList, new Comparator<ImageItem>() {
                    @Override
                    public int compare(ImageItem o1, ImageItem o2) {
                        if (o1.fSize != null && o2.fSize != null)
                            return o2.fSize - o1.fSize;
                        else if (o1.w != null && o2.w != null)
                            return o2.w - o1.w;
                        else if (o1.h != null && o2.h != null)
                            return o2.h - o1.h;
                        else
                            return 0;
                    }
                });

                List<String> ret = new ArrayList<>();
                for (ImageItem img : imgList.subList(0, imgList.size() >= 5 ? 5 : imgList.size())) {
                    if (img.url != null)
                        ret.add(img.url);
                }

                builder.add("imageList", ret);
            }

            // level3
            if (level > 2) {
                builder.add("url", url != null ? url : "");
                builder.add("priceDesc", DataFilter.priceDescFilter(priceDesc));
                builder.add("alias", alias != null ? alias : new ArrayList<>());
                // TODO 暂时兼容两种数据
                //builder.add("desc", (desc != null ? desc : ""));
                if (null != description) {
                    builder.add("desc", (description.desc != null ? description.desc : desc != null ? desc : ""));
                    // web用
                    builder.add("description", description.toJson());
                } else {
                    builder.add("desc", (desc != null ? desc : ""));
                }


            }
        }

        if (level == 1)
            builder.add("addr", (addr != null ? addr.toJson(1) : new BasicDBObject()));
        else if (level > 1)
            builder.add("addr", (addr != null ? addr.toJson(3) : new BasicDBObject()));

        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return toJson(3);
    }
}
