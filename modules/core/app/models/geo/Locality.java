package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.Cuisine;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;
import java.util.Map;

/**
 * 目的地类型
 * <p/>
 * Created by zephyre on 11/21/14.
 */
@Entity
@JsonFilter("localityFilter")
public class Locality extends AizouBaseEntity {

    @Transient
    public static final String FD_ZH_NAME = "zhName";

    @Transient
    public static final String FD_EN_NAME = "enName";

    @Transient
    public static final String FD_ALIAS = "alias";
    @Transient
    public static final String FD_LOCLIST = "locList";
    @Transient
    public static String fnVisitCnt = "visitCnt";
    @Transient
    public static String fnCommentCnt = "commentCnt";
    @Transient
    public static String fnFavorCnt = "favorCnt";
    @Transient
    public static String fnAbroad = "abroad";
    @Transient
    public static String fnCountry = "country";
    @Transient
    public static String fnTags = "tags";

    @Transient
    public static String fnImages = "images";

    @Transient
    public static String fnDesc = "desc";

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
    public static String fnSuperAdm = "superAdm";

    public static String fnImageCnt = "imageCnt";

    @Transient
    public static String fnRemoteTraffic = "remoteTraffic";

    @Transient
    public static String fnLocalTraffic = "localTraffic";

    @Transient
    public static String fnShoppingIntro = "shoppingIntro";

    @Transient
    public static String fnDinningIntro = "dinningIntro";

    @Transient
    public static String fnActivityIntro = "activityIntro";

    @Transient
    public static String fnActivities = "activities";

    @Transient
    public static String fnTips = "tips";

    @Transient
    public static String fnCommodities = "commodities";

    @Transient
    public static String fnCuisines = "cuisines";
    /**
     * 外部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<Map<String, String>> remoteTraffic;
    /**
     * 内部交通信息。每个entry都是一个tip，为HTML格式
     */
    public List<Map<String, String>> localTraffic;
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
     * 是否为热门城市
     */
    public Boolean isHot;
    public List<String> pinyin;
    public List<String> imageList;
    public boolean provCap;
    /**
     * 中文名称
     */
    private String zhName;
    /**
     * 英文名称
     */
    private String enName;
    /**
     * 当地名称
     */
    private String locName;
    /**
     * 别名
     */
    private List<String> alias;
    /**
     * 去过的人数
     */
    private Integer visitCnt;

    /**
     * 其它信息
     */
    //public Map<String, Object> miscInfo;

    /*
      可能废弃的字段-Start
     */
    /**
     * 评论条数
     */
    private Integer commentCnt;
    /**
     * 收藏次数
     */
    private Integer favorCnt;
    /**
     * 热门程度
     */
    private Double hotness;
    /**
     * 评分
     */
    private Double rating;
    /**
     * 是否为境外目的地
     */
    private Boolean abroad;
    /**
     * 经纬度信息
     */
    @Embedded
    private GeoJsonPoint location;
    /**
     * 所在国家（有效字段为_id, zhName, enName和code）
     */
    @Embedded
    private Country country;
    /**
     * 行政区从属链
     */
    private List<Locality> locList;
    /**
     * 父行政区
     */
    private Locality superAdm;
    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 照片
     */
    private List<ImageItem> images;
    /**
     * 简介
     */
    private String desc;
    /**
     * 最佳旅行时间
     */
    private String travelMonth;
    /**
     * 建议游玩时间
     */
    private String timeCostDesc;

    /**
     * 建议游玩时间
     */
    private Integer timeCost;

    public Locality getSuperAdm() {
        return superAdm;
    }

    public void setSuperAdm(Locality superAdm) {
        this.superAdm = superAdm;
    }

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
    }

    public String getZhName() {
        return zhName;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public Boolean getAbroad() {
        return abroad;
    }

    public void setAbroad(Boolean abroad) {
        this.abroad = abroad;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }

    public Integer getVisitCnt() {
        return visitCnt;
    }

    public void setVisitCnt(Integer visitCnt) {
        this.visitCnt = visitCnt;
    }

    public Integer getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(Integer commentCnt) {
        this.commentCnt = commentCnt;
    }

    public Integer getFavorCnt() {
        return favorCnt;
    }

    public void setFavorCnt(Integer favorCnt) {
        this.favorCnt = favorCnt;
    }

    public Double getHotness() {
        return hotness;
    }

    public void setHotness(Double hotness) {
        this.hotness = hotness;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Locality> getLocList() {
        return locList;
    }

    public void setLocList(List<Locality> locList) {
        this.locList = locList;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTravelMonth() {
        return travelMonth;
    }

    public void setTravelMonth(String travelMonth) {
        this.travelMonth = travelMonth;
    }

    public String getTimeCostDesc() {
        return timeCostDesc;
    }

    public void setTimeCostDesc(String timeCostDesc) {
        this.timeCostDesc = timeCostDesc;
    }
}

///**
// * Locality
// *
// * @author Zephyre
// */
//@Entity
//@JsonFilter("localityFilter")
//public class Locality extends TravelPiBaseItem implements ITravelPiFormatter {
//
//    @Transient
//    public static String FD_EN_NAME = "enName";
//
//    @Transient
//    public static String FD_ZH_NAME = "zhName";
//
//    @Transient
//    public static String fnCountry = "country";
//
//    @Transient
//    public static String fnSuperAdm = "superAdm";
//
//    @Transient
//    public static String fnLevel = "level";
//
//    @Transient
//    public static String fnDesc = "desc";
//
//    @Transient
//    public static String fnImages = "images";
//
//    @Transient
//    public static String fnImageList = "imageList";
//
//    @Transient
//    public static final String FD_TAGS = "tags";
//
//    @Transient
//    public static String fnAbroad = "abroad";
//
//    @Transient
//    public static String simpId = "id";
//
//    @Transient
//    public static String simpShortName = "shortName";
//
//    @Transient
//    public static String fnCoords = "coords";
//
//    @Transient
//    public static String fnIsHot = "isHot";
//
//    @Transient
//    public static String fntimeCost = "timeCost";
//
//    @Transient
//    public static String fntravelMonth = "travelMonth";
//
//    @Transient
//    public static String FD_COVER = "cover";
//
//    @Transient
//    public static String fnimageCnt = "imageCnt";
//
//    public String zhName;
//
//    public String enName;
//
//    public String shortName;
//
//    /**
//     * 是否为热门城市
//     */
//    public Boolean isHot;
//
//    public List<String> alias;
//
//    public List<String> pinyin;
//
//    @Embedded
//    public SimpleRef country;
//
//    /**
//     * 是否为国外城市
//     */
//    public Boolean abroad;
//
//    public List<Integer> travelMonth;
//
//    @Constraints.Required
//    public int level;
//
//    @Embedded
//    public Ratings ratings;
//
//    @Embedded
//    public SimpleRef superAdm;
//
//    @Embedded
//    public List<Locality> locList;
//
//    @Embedded
//    public List<SimpleRef> sib;
//
////    @Reference(lazy = true)
////    public List<Locality> siblings;
//
//    public List<String> tags;
//
//    public List<String> imageList;
//
//    public List<ImageItem> images;
//
//    public boolean provCap;
//
//    public Integer baiduId;
//
//    public Integer qunarId;
//
//    public Integer areaCode;
//
//    @Embedded
//    public GeoJsonPoint location;
//
//    @Embedded
//    public Coords coords;
//
//    @Embedded
//    public Coords bCoords;
//
//    public String desc;
//
//    public Double timeCost;
//
//    public String cover;
//
//    /**
//     * 该locality对应路线的
//     */
//    public Integer relPlanCnt;
//
//    /**
//     * 去掉末尾的省市县等名字。
//     *
//     * @param name
//     * @return
//     */
//    public static String stripLocName(String name) {
//        Pattern pattern = Pattern.compile("(族|自治)");
//
//        while (true) {
//            boolean stripped = false;
//            Matcher m = pattern.matcher(name);
//            int start = -1;
//            if (m.find())
//                start = m.start();
//            int idx = start;
//            final int endIdx = start;
//
//            if (idx != -1) {
//                // 尝试找出是什么族
//                String minorities = (String) GlobalInfo.metadata.get("minoritiesDesc");
//                idx--;
//                while (idx > 0) {
//                    Matcher matcher = Pattern.compile(String.format("\\|%s\\|", name.substring(idx, endIdx) + "族")).matcher(minorities);
//                    if (matcher.find()) {
//                        String tmp = name.substring(0, idx);
//                        if (tmp.length() > 1) {
//                            name = tmp;
//                            stripped = true;
//                        } else {
//                            name = name.substring(0, endIdx);
//                            stripped = false;
//                        }
//                        break;
//                    }
//                    idx--;
//                }
//            }
//
//            if (!stripped)
//                break;
//        }
//
//        pattern = Pattern.compile("(市|县|省|直辖市|自治县|自治区|地区)$");
//        String result = pattern.matcher(name).replaceAll("");
//        if (result.length() == 1)
//            // 如果去掉县、市等字以后，名称只剩一个字，则不被允许
//            result = name;
//        return result;
//    }
//
//    public String getName() {
//        if (zhName == null)
//            return "";
//        else
//            return stripLocName(zhName);
//    }
//
//    public List<String> getImages() {
//        if (imageList == null)
//            return new ArrayList();
//        else {
//            return imageList;
//        }
//    }
//
//    public String getDesc() {
//        if (desc == null)
//            return "";
//        else
//            return StringUtils.abbreviate(desc, Constants.ABBREVIATE_LEN);
//    }
//
//    public String getEnName() {
//        if (enName == null)
//            return "";
//        else
//            return StringUtils.capitalize(enName);
//    }
//
//    public String getShortName() {
//        if (shortName == null)
//            return stripLocName(zhName);
//        else
//            return StringUtils.capitalize(shortName);
//    }
//
//    public Boolean getAbroad() {
//        return abroad != null && abroad;
//    }
//
//    public Double getTimeCost() {
//        return timeCost != null ? timeCost : 0;
//    }
//
//    public Integer getImageCnt() {
//        return images != null ? images.size() : 0;
//    }
//
//    public String getCover() {
//        return cover != null ? cover : "";
//    }
//
//    // TODO
//    public String getTravelMonth() {
//        return travelMonth != null ? "" : "";
//    }
//
//    public JsonNode getJsonNode() {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        builder.add("_id", id.toString()).add("name", zhName).add("level", level);
//
//        builder.add("parent", null);
//
//        return Json.toJson(builder.get());
//    }
//
//    @Override
//    public JsonNode toJson() {
//        return toJson(1);
//    }
//
//    public JsonNode toJson(int detailLevel) {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        if (shortName == null)
//            shortName = stripLocName(zhName);
//        builder.add("_id", id.toString()).add("level", level)
//                .add("name", StringUtils.capitalize(shortName))
//                .add("fullName", StringUtils.capitalize(zhName))
//                .add("enName", (enName != null ? StringUtils.capitalize(enName) : ""));
//
//        if (superAdm != null) {
//            String fullName = superAdm.zhName;
//            superAdm.zhName = stripLocName(fullName);
//            ObjectNode node = (ObjectNode) superAdm.toJson();
//            node.put("fullName", fullName);
//            builder.add("parent", node);
//        } else
//            builder.add("parent", new BasicDBObject());
//
//        builder.add("abroad", (abroad != null && abroad));
//
////        if (country != null) {
////            ObjectNode node = (ObjectNode) country.toJson();
////            builder.add("countryDetails", node);
////        } else
////            builder.add("countryDetails", new BasicDBObject());
//
//        if (coords != null) {
//            if (coords.blat != null) builder.add("blat", coords.blat);
//            if (coords.blng != null) builder.add("blng", coords.blng);
//            if (coords.lat != null) builder.add("lat", coords.lat);
//            if (coords.lng != null) builder.add("lng", coords.lng);
//        }
//
//        if (detailLevel > 1) {
//            builder.add("desc", (desc != null && !desc.isEmpty()) ? desc : "");
//
//            if (ratings != null)
//                builder.add("ratings", ratings.toJson());
//            else
//                builder.add("ratings", new BasicDBObject());
//
//
//            // 如果存在更高阶的images字段，则使用之
//            if (images != null && !images.isEmpty()) {
//                List<ImageItem> imgList = new ArrayList<>();
//                for (ImageItem img : images) {
//                    if (img.enabled != null && !img.enabled)
//                        continue;
//                    imgList.add(img);
//                }
//
//                List<String> ret = new ArrayList<>();
//                for (ImageItem img : imgList.subList(0, imgList.size() >= 5 ? 5 : imgList.size())) {
//                    if (img.url != null)
//                        ret.add(img.url);
//                }
//
//                builder.add("imageList", ret);
//            }
//            //builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? imageList : new ArrayList<>());
//            builder.add("tags", (tags != null && !tags.isEmpty()) ? tags : new ArrayList<>());
//        }
//
//        return Json.toJson(builder.get());
//    }
//}
