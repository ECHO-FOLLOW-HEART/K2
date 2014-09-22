package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import play.libs.Json;

/**
 * Created by topy on 2014/9/13.
 */
public class Recommendation extends TravelPiBaseItem implements ITravelPiFormatter {

    /**
     * 名称
     */
    public String name;

    /**
     * 热门景点
     */
    public Integer hotVs;

    /**
     * 热门城市
     */
    public Integer hotCity;

    /**
     * 新鲜出炉
     */
    public Integer newItemWeight;

    /**
     * 不可不去
     */
    public Integer mustGoWeight;

    /**
     * 小编推荐
     */
    public Integer editorWeight;

    /**
     * 人气之旅
     */
    public Integer popularityWeight;

    /**
     * 理由
     */
    public String reason;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("name", name).add("id", id).add(reason, reason == null ? "" : reason);
        return Json.toJson(builder.get());
    }

}
