package models.geo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
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
    public static String simpZhCont = "zhCont";
    @Transient
    public static String simpEnCont = "enCont";
    @Transient
    public static String simpEnRegion = "enRegion";
    @Transient
    public static String SimpZhRegion = "zhRegion";
    @Transient
    public static String simpZhName = "zhName";
    @Transient
    public static String simpEnName = "enName";
    @Transient
    public static String simpIsHot = "isHot";
    @Transient
    public static String simpId = "id";
    /**
     * ISO 3166-2标准的国家代码
     */
    public String code;

    /**
     * ISO 3166-3标准的国家代码
     */
    public String code3;

    /**
     * 标准的ISO-Numeric代码
     */
    public String isoNum;

    /**
     * FIPS国家代码
     */
    public String fips;

    /**
     * 所在大洲的中文名称
     */
    public String zhCont;

    /**
     * 所在大洲的英文名称
     */
    public String enCont;

    /**
     * 所在大洲的代码
     */
    public String contCode;

    /**
     * 所在区域的中文名称
     */
    public String zhRegion;

    /**
     * 所在区域的英文名称
     */
    public String enRegion;

    /**
     * 中文名称
     */
    public String zhName;

    /**
     * 英文名称
     */
    public String enName;

    /**
     * 首都
     */
    public String enCapital;

    /**
     * 面积（平方千米）
     */
    public Integer area;

    /**
     * 人口数量
     */
    public Integer population;

    /**
     * 别名
     */
    public List<String> alias;

    /**
     * 默认货币
     */
    public String currencyCode;

    /**
     * 货币英文名称
     */
    public String currencyEnName;

    /**
     * 货币中文名称
     */
    public String currencyZhName;

    /**
     * 电话的国家代码
     */
    public Integer dialCode;

    /**
     * 语言
     */
    public List<String> lang;

    /**
     * 相邻的国家
     */
    public List<String> neighbours;

    /**
     * 是否为热门旅游目的地国家
     */
    public Boolean isHot;

    /**
     * 对应的穷游代码
     */
    public Integer qyerId;

    public String getId() {
        return id.toString();
    }

    public String getZhCont() {
        if (zhCont == null)
            return "";
        else
            return zhCont;
    }

    public String getEnCont() {
        if (enCont == null)
            return "";
        else
            return enCont;
    }

    public String getZhRegion() {
        if (zhRegion == null)
            return "";
        else
            return zhRegion;
    }

    public String getEnRegion() {
        if (enRegion == null)
            return "";
        else
            return enRegion;
    }

    public String getEnName() {
        if (enName == null)
            return "";
        else
            return enName;
    }

    public String getZhName() {
        if (zhName == null)
            return "";
        else
            return zhName;
    }

    public Boolean getIsHot() {
        if (isHot == null)
            return false;
        else
            return true;
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
