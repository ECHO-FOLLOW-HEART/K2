package models.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import models.misc.SimpleRef;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 列车路线。
 *
 * @author Zephyre
 */
@Entity
public class TrainRoute extends AbstractRoute {
    public Map<String, Double> price;
    /**
     * 列车车次的类型。包括但不限于：
     * T：特快
     * D：动车
     * G：高铁
     */
    public String type;

    public List<TrainEntry> details;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", getId().toString()).add("code", code);

        for (Map.Entry<String, String> entry : new HashMap<String, String>() {
            {
                put("arrStop", "arrTrainStation");
                put("depStop", "depTrainStation");
            }
        }.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            SimpleRef val = null;
            try {
                val = (SimpleRef) TrainRoute.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            //PC_Chen:return {} instead of ""
            builder.add(v, val != null ? val.toJson() : new HashMap<>());
        }

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
//        for (String k : new String[]{"depTime", "arrTime"}) {
//            Date val = null;
//            try {
//                val = (Date) AirRoute.class.getField(k).get(this);
//            } catch (NoSuchFieldException | IllegalAccessException ignored) {
//            }
//            builder.add(k, val != null ? fmt.format(val) : "");
//        }

        try {
            Date depDate = (Date) AirRoute.class.getField("depTime").get(this);
            String dbSepDate = fmt.format(depDate);
            builder.add("depTime", depDate != null ? dbSepDate : "");

            Date arrDate = (Date) AirRoute.class.getField("arrTime").get(this);
            String dbArrDate = fmt.format(arrDate);
            builder.add("arrTime", depDate != null ? dbArrDate : "");

            // 花费时间：不取DB中的timeCost字段，而是计算得出:分钟数
            int days = (int) (arrDate.getTime() - depDate.getTime()) / (60 * 1000);
            builder.add("timeCost", days);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }


        super.addColumn(builder, TrainRoute.class, "totalDist", "distance", "type");

        // 价格列表
        BasicDBObjectBuilder priceBuilder = BasicDBObjectBuilder.start();
        if (null != price) {
            for (Map.Entry<String, Double> entry : price.entrySet()) {
                String k = entry.getKey();
                Double v = entry.getValue();

                //PC_Chen:return {} instead of ""
                if (v != null)
                    priceBuilder.add(k, v);
            }
        }
        builder.add("priceList", priceBuilder.get());

        //最低价格
        DBObject priceList = priceBuilder.get();
        // 最低票价
        if (null != priceList && ((BasicDBObject) priceList).size() > 0) {
            builder.add("price", Collections.min(priceList.toMap().values()));
        }

        return Json.toJson(builder.get());
    }


}
