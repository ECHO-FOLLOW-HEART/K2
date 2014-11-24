package utils.formatter.travelpi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utils.formatter.JsonFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 旅行派的formatter基类
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public abstract class TravelPiBaseFormatter implements JsonFormatter {

    protected Set<String> stringFields;

    protected Set<String> listFields;

    protected Set<String> mapFields;

    protected ObjectNode postProcess(ObjectNode result) {
        // 处理字符串字段
        if (stringFields != null) {
            for (String key : stringFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, "");
            }
        }

        // 处理列表字段
        if (listFields != null) {
            for (String key : listFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, Json.toJson(new ArrayList<>()));
            }
        }

        // 处理字典字段
        if (mapFields != null) {
            for (String key : mapFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, Json.toJson(new HashMap<>()));
            }
        }

        // 处理id
        result.put("_id", result.get("id"));

        result.put("relVs", Json.toJson(new ArrayList<>()));

        return result;
    }
}
