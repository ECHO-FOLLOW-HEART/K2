package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 国家模型。主键为ISO 3166-2标准的国家代码。
 *
 * @author Zephyre
 */
@JsonFilter("countryFilter")
@Entity
public class Country extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static String fnCode = "code";

    @Transient
    public static String fnCode3 = "code3";

    @Transient
    public static String fnAlias = "alias";

    @Transient
    public static String fnIsHot = "isHot";

//    @Transient
//    public static String simpZhCont = "zhCont";
//
//    @Transient
//    public static String simpEnCont = "enCont";
//
//    @Transient
//    public static String simpEnRegion = "enRegion";
//
//    @Transient
//    public static String SimpZhRegion = "zhRegion";

    @Transient
    public static String fnZhName = "zhName";

    @Transient
    public static String fnEnName = "enName";

    @Transient
    public static String fnId = "id";

    /**
     * ISO 3166-2标准的国家代码
     */
    private String code;

    /**
     * ISO 3166-3标准的国家代码
     */
    private String code3;

    /**
     * 标准的ISO-Numeric代码
     */
    private String isoNum;

    /**
     * FIPS国家代码
     */
    public String fips;

    /**
     * 所在大洲的中文名称
     */
    private String zhCont;

    /**
     * 所在大洲的英文名称
     */
    private String enCont;

    /**
     * 所在大洲的代码
     */
    private String contCode;

    /**
     * 所在区域的中文名称
     */
    private String zhRegion;

    /**
     * 所在区域的英文名称
     */
    private String enRegion;

    /**
     * 中文名称
     */
    private String zhName;

    /**
     * 英文名称
     */
    private String enName;

    /**
     * 首都
     */
    private String enCapital;

    /**
     * 面积（平方千米）
     */
    private Integer area;

    /**
     * 人口数量
     */
    public Integer population;

    /**
     * 别名
     */
    private List<String> alias;

    /**
     * 默认货币
     */
    private String currencyCode;

    /**
     * 货币英文名称
     */
    private String currencyEnName;

    /**
     * 货币中文名称
     */
    private String currencyZhName;

    /**
     * 电话的国家代码
     */
    private Integer dialCode;

    /**
     * 语言
     */
    private List<String> lang;

    /**
     * 相邻的国家
     */
    private List<String> neighbours;

    /**
     * 是否为热门旅游目的地国家
     */
    private Boolean isHot;

    /**
     * 相邻的国家
     */
    public List<String> citys;

    /**
     * 图片
     */
    public List<ImageItem> images;

    public String getId() {
        return id.toString();
    }

    public String getZhCont() {
        return zhCont != null ? zhCont : "";
    }

    public String getEnCont() {
        return enCont != null ? enCont : "";
    }

    public String getZhRegion() {
        return zhRegion != null ? zhRegion : "";
    }

    public String getEnRegion() {
        return enRegion != null ? enRegion : "";
    }

    public String getEnName() {
        return enName != null ? enName : "";
    }

    public String getZhName() {
        return zhName != null ? zhName : "";
    }

    public Boolean getIsHot() {
        return this.isHot;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode3() {
        return code3;
    }

    public void setCode3(String code3) {
        this.code3 = code3;
    }

    public String getIsoNum() {
        return isoNum;
    }

    public void setIsoNum(String isoNum) {
        this.isoNum = isoNum;
    }

    public String getFips() {
        return fips;
    }

    public void setFips(String fips) {
        this.fips = fips;
    }

    public void setZhCont(String zhCont) {
        this.zhCont = zhCont;
    }

    public void setEnCont(String enCont) {
        this.enCont = enCont;
    }

    public String getContCode() {
        return contCode != null ? contCode : "";
    }

    public void setContCode(String contCode) {
        this.contCode = contCode;
    }

    public void setZhRegion(String zhRegion) {
        this.zhRegion = zhRegion;
    }

    public void setEnRegion(String enRegion) {
        this.enRegion = enRegion;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getEnCapital() {
        return enCapital != null ? enCapital : "";
    }

    public void setEnCapital(String enCapital) {
        this.enCapital = enCapital;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public List<String> getAlias() {
        return alias != null ? alias : new ArrayList<String>();
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyEnName() {
        return currencyEnName;
    }

    public void setCurrencyEnName(String currencyEnName) {
        this.currencyEnName = currencyEnName;
    }

    public String getCurrencyZhName() {
        return currencyZhName;
    }

    public void setCurrencyZhName(String currencyZhName) {
        this.currencyZhName = currencyZhName;
    }

    public Integer getDialCode() {
        return dialCode;
    }

    public void setDialCode(Integer dialCode) {
        this.dialCode = dialCode;
    }

    public List<String> getLang() {
        return lang;
    }

    public void setLang(List<String> lang) {
        this.lang = lang;
    }

    public List<String> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<String> neighbours) {
        this.neighbours = neighbours;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        try {
            for (String k : new String[]{"id", "code", "code3", "zhName", "enName", "zhCont", "enCont"}) {
                Object val = Country.class.getField(k).get(this);
                builder.add(k, val != null ? val.toString() : "");
            }
            builder.add("isHot", isHot != null ? isHot : false);
            builder.add("alias", Json.toJson(alias != null ? alias : new ArrayList<>()));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return Json.toJson(builder.get());
    }
}
