package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.geo.*;
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

import java.util.*;

/**
 * POI的基类
 *
 * @author Zephyre
 *         Created by zephyre on 7/16/14.
 */
@JsonFilter("abstractPOIFilter")
// For deserialize a JSON
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ViewSpot.class, name = "vs"),
        @JsonSubTypes.Type(value = Hotel.class, name = "hotel"),
        @JsonSubTypes.Type(value = Restaurant.class, name = "restaurant"),
        @JsonSubTypes.Type(value = Shopping.class, name = "shopping")
})
public abstract class AbstractPOI extends TravelPiBaseItem implements ITravelPiFormatter {

    /**
     * 标识POI的种类，jackson反序列还用
     */
    public String type;

    @Transient
    public static String simpID = "id";

    @Transient
    public static final String FD_NAME = "name";

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_DESC = "desc";

    @Transient
    public static final String FD_IMAGES = "images";

    @Transient
    public static String detAddr = "addr";

    @Transient
    public static String FD_LOCALITY = "locality";

    @Transient
    public static String detDesc = "description";

    @Transient
    public static String FD_PRICE = "price";

    @Transient
    public static String FD_PRICE_DESC = "priceDesc";

    @Transient
    public static String detContact = "contact";

    @Transient
    public static String detOpenTime = "openTime";

    @Transient
    public static String fnAlias = "alias";

    @Transient
    public static String detAlias = "alias";

    @Transient
    public static String detTargets = "targets";

    @Transient
    public static String simpEnName = "enName";

    @Transient
    public static String simpRating = "rating";

    @Transient
    public static String fnLocation = "location";

    @Transient
    public static String simpAddress = "address";

    @Transient
    public static String simpTelephone = "telephone";

    @Transient
    public static String fnHotness = "hotness";

    @Transient
    public static String fnRating = "rating";

    @Transient
    public static String simpCountry = "country";

    @Transient
    public static String simplocList = "locList";

    @Transient
    public static final String FD_TAGS = "tags";

    @Transient
    public static String fnMoreCommentsUrl = "fnMoreCommentsUrl";

    @Embedded
    public CheckinRatings ratings;

    @Embedded
    public Contact contact;

    @Embedded
    public Address addr;

    @Embedded
    public Coords coords;

    /**
     * 是否位于国外
     */
    public Boolean abroad;

    public String name;

    public String zhName;

    public String enName;

    public String url;

    public Double price;

    public String priceDesc;

    public String desc;

    /**
     * 坐标
     */
    private GeoJsonPoint location;

    /**
     * 所在目的地
     */
    private Locality locality;

    /**
     * 电话
     */
    public List<String> tel;

    /**
     * 网址
     */
    public String website;

    /**
     * 开放时间描述
     */
    public String openTime;

    public Integer openHour;

    public Integer closeHour;

    @Embedded
    public Description description;

    public String trafficInfo;

    public List<ImageItem> images;

    public String cover;

    public List<String> tags;

    public List<String> alias;

    /**
     * POI所在的行政区划。
     */
    public List<ObjectId> targets;

    /**
     * 表示该POI的来源。注意：一个POI可以有多个来源。
     * 示例：
     * <p/>
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


    public Double rating;

    /**
     * 热门程度
     */
    private Double hotness;

    /**
     * 交通指南URL
     */
    public String trafficInfoUrl;

    /**
     * 旅行指南URL
     */
    public String guideUrl;

    /**
     * 防坑攻略URL
     */
    public String kengdieUrl;

    /**
     * 地址
     */
    public String address;

    /**
     * 电话
     */
    public String telephone;

    /**
     * 国家
     */
    public Country country;

    /**
     * 从属行政关系
     */
    public List<Locality> locList;

    public Map<String, Object> miscInfo;

    public String moreCommentsUrl;

    public static List<String> getRetrievedFields(int level) {
        switch (level) {
            case 1:
                return new ArrayList<>(Arrays.asList("zhName", "enName", "rating", "images", "id"));
            case 2:
                return new ArrayList<>(Arrays.asList("zhName", "enName", "rating", "images", "id", "desc", "images",
                        "tags", "location", "locList"));
            case 3:
                return new ArrayList<>(Arrays.asList("name", "addr", "ratings", "desc", "images", "tags", "contact",
                        "url", "price", "priceDesc", "alias", "locality", "location"));
        }
        return new ArrayList<>();
    }

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }

    public Locality getLocality() {
        return locality;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    public Double getHotness() {
        return hotness;
    }

    public void setHotness(Double hotness) {
        this.hotness = hotness;
    }

    public String getTrafficInfoUrl() {
        return trafficInfoUrl;
    }

    public String getGuideUrl() {
        return guideUrl;
    }

    public String getKengdieUrl() {
        return priceDesc;
    }

    public String getDesc() {
        if (desc == null)
            return "";
        else
            return StringUtils.abbreviate(desc, Constants.ABBREVIATE_LEN);
    }

    public String getEnName() {
        return enName;
    }


    public Contact getContact() {
        return contact;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getTrafficInfo() {
        if (trafficInfo == null)
            return "";
        else
            return trafficInfo;
    }

    public List<ImageItem> getImages() {
        if (images == null)
            return new ArrayList();
        else
            return images;
    }

    public JsonNode toJson(int level) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // level1
        builder.add("_id", getId().toString());
        if (ratings == null)
            ratings = new CheckinRatings();
        builder.add("ratings", ratings.toJson());
        builder.add("name", (name != null ? name : ""));

        // level2
        // TODO 完善POI所使用的Formatter
        if (level > 1) {
            for (Map.Entry<String, String> entry : new HashMap<String, String>() {
                {
//                    put("imageList", Locality.fnImageList);
                    put("tags", Locality.fnTags);
                }
            }.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                try {
                    Object val = AbstractPOI.class.getField(v).get(this);
                    boolean isNull = (val == null);
                    if (val != null && val instanceof Collection)
                        isNull = ((Collection) val).isEmpty();
                    builder.add(k, (isNull ? new ArrayList<>() : val));
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    builder.add(k, new ArrayList<>());
                }
            }

            // 如果存在更高阶的images字段，则使用之
            if (images != null && !images.isEmpty()) {
                List<ImageItem> imgList = new ArrayList<>();
                for (ImageItem img : images) {
                    imgList.add(img);
                }

                // 简单的挑选图像挑选算法：选取清晰度最高的5张图像。
                Collections.sort(imgList, new Comparator<ImageItem>() {
                    @Override
                    public int compare(ImageItem o1, ImageItem o2) {
                        if (o1.getSize() != null && o2.getSize() != null)
                            return o2.getSize() - o1.getSize();
                        else if (o1.getW() != null && o2.getW() != null)
                            return o2.getW() - o1.getW();
                        else if (o1.getH() != null && o2.getH() != null)
                            return o2.getH() - o1.getH();
                        else
                            return 0;
                    }
                });
                List<String> ret = new ArrayList<>();
                for (ImageItem img : imgList.subList(0, imgList.size() >= 5 ? 5 : imgList.size())) {
                    if (img.getUrl() != null)
                        ret.add(img.getUrl());
                }

                builder.add("imageList", ret);
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

            // level3
            if (level > 2) {
                builder.add("url", url != null ? url : "");
                builder.add("priceDesc", DataFilter.priceDescFilter(priceDesc));
                if (alias != null) {
                    Set<String> aliasSet = new HashSet<>();
                    for (String val : alias) {
                        if (val != null && name != null && !val.equals(name))
                            aliasSet.add(val);
                    }
                    builder.add("alias", Arrays.asList(aliasSet.toArray(new String[aliasSet.size()])));
                } else
                    builder.add("alias", new ArrayList<>());
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

        // 历史性兼容
        if (addr == null) {
            addr = new Address();
            if (location != null) {
                addr.coords = new Coords();
                double[] c = location.getCoordinates();
                addr.coords.lng = c[0];
                addr.coords.lat = c[1];
            }
            if (address != null) {
                addr.address = address;
            }
        }
        if (level == 1)
            builder.add("addr", (addr != null ? addr.toJson(1) : new BasicDBObject()));
        else if (level > 1)
            builder.add("addr", (addr != null ? addr.toJson(3) : new BasicDBObject()));


        DBObject o = builder.get();
        if (o.get("imageList") == null)
            o.put("imageList", new ArrayList<>());

        return Json.toJson(o);
    }

    @Override
    public JsonNode toJson() {
        return toJson(3);
    }
}
