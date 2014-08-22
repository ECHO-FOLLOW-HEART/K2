package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

/**
 * POI的评论信息。
 *
 * @author Zephyre
 */
@Embedded
public class Ratings implements ITravelPiFormatter {
    public Integer score;

    public Integer viewCnt;

    public Integer favorCnt;

    public Integer dinningIdx;

    public Integer shoppingIdx;

    public Double ranking;

    /**
     * 百度搜索出来的结果数量。
     */
    public Integer baiduIndex;

    /**
     * 去哪儿的评分。
     */
    public Integer qtScore;

    /**
     * 百度旅行的评分。
     */
    public Double baiduScore;

    /**
     * 是否为推荐POI。
     */
    public Boolean recommended;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        // TODO hardcode here
//        if (ranking == null)
//            ranking = 0.8;
        if (viewCnt == null)
            viewCnt = (score != null ? score % 500 : 23);// (new Random()).nextInt(324);
        if (favorCnt == null)
            favorCnt = (score != null ? score * 9 % 439 : 87);//(new Random()).nextInt(324);

        for (String k : new String[]{"score", "dinningIdx", "shoppingIdx", "viewCnt", "favorCnt", "ranking",
                "baiduScore", "qtScore", "recommended"}) {
            Object val = null;
            try {
                val = Ratings.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            if (val != null)
                builder.add(k, val);
        }

        // TODO 保证ranking>0.3
        if (ranking == null)
            ranking = 0.0;
        ranking = ranking * 0.7 + 0.3;
        builder.add("ranking", ranking);


        return Json.toJson(builder.get());
    }
}
