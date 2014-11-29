package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import misc.GlobalInfo;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import models.misc.Ratings;
import models.misc.SimpleRef;
import models.poi.Cuisine;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;
import utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locality
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("localityFilter")
public class Locality extends TravelPiBaseItem implements ITravelPiFormatter {

    @Transient
    public static String fnId = "id";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnCountry = "country";

    @Transient
    public static String fnSuperAdm = "superAdm";

    @Transient
    public static String fnDesc = "desc";

    @Transient
    public static String fnImages = "images";

    @Transient
    public static String fnImageList = "imageList";

    @Transient
    public static final String fnTags = "tags";

    @Transient
    public static String fnAbroad = "abroad";

    @Transient
    public static String simpId = "id";

    @Transient
    public static String fnLocation = "location";

    @Transient
    public static String fnHotness = "hotness";

    @Transient
    public static String fnRating = "rating";

    @Transient
    public static String fnTimeCost = "timeCost";

    @Transient
    public static String fnTimeCostDesc = "timeCostDesc";

    @Transient
    public static String fnTravelMonth = "travelMonth";

    @Transient
    public static String fnCover = "cover";

    @Transient
    public static String fnImageCnt = "imageCnt";

    @Transient
    public static String fnLevel = "level";


    public String zhName;

    public String enName;

    public String shortName;

    public String desc;

    public List<ImageItem> images;

    /**
     * 是否为国外城市
     */
    public Boolean abroad;

    /**
     * 坐标
     */
    @Embedded
    public GeoJsonPoint location;

    @Embedded
    public SimpleRef country;

    public List<String> tags;

    /**
     * 该locality对应路线的
     */
    public Integer relPlanCnt;
    /**
     * 建议游玩天数
     */
    public Double timeCost;

    /**
     * 建议游玩天数描述
     */
    public String timeCostDesc;

    /**
     * 建议游玩月份
     */
    public List<Integer> travelMonth;

    /* 桃子旅行新加字段 */
    /**
     * 评价
     */
    public Double rating;
    /**
     * 热门程度
     */
    public Double hotness;

    /**
     * 收藏数量
     */
    public Integer favorCnt;

    /**
     * 去过人数
     */
    public Integer visitCnt;

    /**
     * 评价数量
     */
    public Integer commentCnt;

    /**
     * 外部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<String> remoteTraffic;

    /**
     * 内部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<String> localTraffic;

    /**
     * 购物综述，HTML格式
     */
    public String shoppingIntro;

    /**
     * 特产
     */
    public List<Commodities> commodities;

    /**
     * 美食综述，HTML格式
     */
    public String dinningIntro;

    /**
     * 特色菜式
     */
    public List<Cuisine> cuisines;

    /**
     * 活动综述
     */
    public String activityIntro;

    /**
     * 活动
     */
    public List<Activities> activities;

    /**
     * 小贴士
     */
    public List<Tip> tips;

    /**
     * 其它信息
     */
    public Map<String, Object> miscInfo;

    /*
      可能废弃的字段-Start
     */
    /**
     * 是否为热门城市
     */
    public Boolean isHot;

    public List<String> alias;

    public List<String> pinyin;

    @Constraints.Required
    public int level;

    @Embedded
    public Ratings ratings;

    @Embedded
    public SimpleRef superAdm;

    @Embedded
    public List<SimpleRef> sib;

//    @Reference(lazy = true)
//    public List<Locality> siblings;

    public List<String> imageList;

    public boolean provCap;

    public Integer baiduId;

    public Integer qunarId;

    public Integer areaCode;

    @Embedded
    public Coords coords;

    public String cover;

    @Embedded
    public Coords bCoords;

    /* 可能废弃的字段-End */

    /**
     * 去掉末尾的省市县等名字。
     *
     * @param name
     * @return
     */
    public static String stripLocName(String name) {
        Pattern pattern = Pattern.compile("(族|自治)");

        while (true) {
            boolean stripped = false;
            Matcher m = pattern.matcher(name);
            int start = -1;
            if (m.find())
                start = m.start();
            int idx = start;
            final int endIdx = start;

            if (idx != -1) {
                // 尝试找出是什么族
                String minorities = (String) GlobalInfo.metadata.get("minoritiesDesc");
                idx--;
                while (idx > 0) {
                    Matcher matcher = Pattern.compile(String.format("\\|%s\\|", name.substring(idx, endIdx) + "族")).matcher(minorities);
                    if (matcher.find()) {
                        String tmp = name.substring(0, idx);
                        if (tmp.length() > 1) {
                            name = tmp;
                            stripped = true;
                        } else {
                            name = name.substring(0, endIdx);
                            stripped = false;
                        }
                        break;
                    }
                    idx--;
                }
            }

            if (!stripped)
                break;
        }

        pattern = Pattern.compile("(市|县|省|直辖市|自治县|自治区|地区)$");
        String result = pattern.matcher(name).replaceAll("");
        if (result.length() == 1)
            // 如果去掉县、市等字以后，名称只剩一个字，则不被允许
            result = name;
        return result;
    }

    public String getName() {
        if (zhName == null)
            return "";
        else
            return stripLocName(zhName);
    }

    public List<String> getImages() {
        if (imageList == null)
            return new ArrayList();
        else {
            return imageList;
        }
    }

    public String getDesc() {
        if (desc == null)
            return "";
        else
            return StringUtils.abbreviate(desc, Constants.ABBREVIATE_LEN);
    }

    public String getEnName() {
        if (enName == null)
            return "";
        else
            return StringUtils.capitalize(enName);
    }

    public String getShortName() {
        if (shortName == null)
            return stripLocName(zhName);
        else
            return StringUtils.capitalize(shortName);
    }

    public Boolean getAbroad() {
        return abroad != null && abroad;
    }

    public Double getTimeCost() {
        return timeCost != null ? timeCost : 0;
    }

    public Integer getImageCnt() {
        return images != null ? images.size() : 0;
    }

    public String getCover() {
        return cover != null ? cover : "";
    }

    // TODO
    public String getTravelMonth() {
        return travelMonth != null ? "" : "";
    }

    public JsonNode getJsonNode() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString()).add("name", zhName).add("level", level);

        builder.add("parent", null);

        return Json.toJson(builder.get());
    }

    @Override
    public JsonNode toJson() {
        return toJson(1);
    }

    public JsonNode toJson(int detailLevel) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        if (shortName == null)
            shortName = stripLocName(zhName);
        builder.add("_id", id.toString()).add("level", level)
                .add("name", StringUtils.capitalize(shortName))
                .add("fullName", StringUtils.capitalize(zhName))
                .add("enName", (enName != null ? StringUtils.capitalize(enName) : ""));

        if (superAdm != null) {
            String fullName = superAdm.zhName;
            superAdm.zhName = stripLocName(fullName);
            ObjectNode node = (ObjectNode) superAdm.toJson();
            node.put("fullName", fullName);
            builder.add("parent", node);
        } else
            builder.add("parent", new BasicDBObject());

        builder.add("abroad", (abroad != null && abroad));

//        if (country != null) {
//            ObjectNode node = (ObjectNode) country.toJson();
//            builder.add("countryDetails", node);
//        } else
//            builder.add("countryDetails", new BasicDBObject());

        if (coords != null) {
            if (coords.blat != null) builder.add("blat", coords.blat);
            if (coords.blng != null) builder.add("blng", coords.blng);
            if (coords.lat != null) builder.add("lat", coords.lat);
            if (coords.lng != null) builder.add("lng", coords.lng);
        }

        if (detailLevel > 1) {
            builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");

            if (ratings != null)
                builder.add("ratings", ratings.toJson());
            else
                builder.add("ratings", new BasicDBObject());


            // 如果存在更高阶的images字段，则使用之
            if (images != null && !images.isEmpty()) {
                List<ImageItem> imgList = new ArrayList<>();
                for (ImageItem img : images) {
                    if (img.enabled != null && !img.enabled)
                        continue;
                    imgList.add(img);
                }

                List<String> ret = new ArrayList<>();
                for (ImageItem img : imgList.subList(0, imgList.size() >= 5 ? 5 : imgList.size())) {
                    if (img.url != null)
                        ret.add(img.url);
                }

                builder.add("imageList", ret);
            }
            //builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? imageList : new ArrayList<>());
            builder.add("tags", (tags != null && !tags.isEmpty()) ? tags : new ArrayList<>());
        }

        return Json.toJson(builder.get());
    }
}
