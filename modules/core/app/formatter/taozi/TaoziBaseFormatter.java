package formatter.taozi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.JsonFormatter;
import models.geo.GeoJsonPoint;
import org.bson.types.ObjectId;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 旅行派的formatter基类
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public abstract class TaoziBaseFormatter implements JsonFormatter {

    protected Set<String> stringFields = new HashSet<>();

    protected Set<String> listFields = new HashSet<>();

    protected Set<String> mapFields = new HashSet<>();

    protected ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        SimpleModule objectIdModule = new SimpleModule();
        objectIdModule.addSerializer(ObjectId.class, new ObjectIdSerializer());
        mapper.registerModule(objectIdModule);

        FilterProvider filters = new SimpleFilterProvider().addFilter("geoJsonPointFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(GeoJsonPoint.FD_COORDS));
        mapper.setFilters(filters);

        return mapper;
    }

    protected ObjectNode postProcess(ObjectNode result) {
        // 处理字符串字段
        for (String key : stringFields) {
            if (result.get(key) == null || result.get(key).isNull())
                result.put(key, "");
        }

        // 处理列表字段
        for (String key : listFields) {
            if (result.get(key) == null || result.get(key).isNull())
                result.put(key, Json.toJson(new ArrayList<>()));
        }

        // 处理字典字段
        for (String key : mapFields) {
            if (result.get(key) == null || result.get(key).isNull())
                result.put(key, Json.toJson(new HashMap<>()));
        }

        return result;
    }
}
