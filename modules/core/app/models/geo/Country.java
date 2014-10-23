package models.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 国家模型。主键为ISO 3166-2标准的国家代码。
 *
 * @author Zephyre
 */
@Entity
public class Country extends TravelPiBaseItem implements ITravelPiFormatter {
    /**
     * ISO 3166-2标准的国家代码
     */
    public String code;

    /**
     * ISO 3166-3标准的国家代码
     */
    public String code3;

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
     * 别名
     */
    public List<String> alias;

    /**
     * 默认货币
     */
    public String defCurrency;

    /**
     * 是否为热门旅游目的地国家
     */
    public Boolean isHot;

    /**
     * 对应的穷游代码
     */
    public Integer qyerId;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        try {
            for (String k : new String[]{"id", "code", "code3", "zhName", "enName", "zhCont", "enCont", "defCurrency"}) {
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
