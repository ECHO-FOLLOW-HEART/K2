package formatter.taozi;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.JsonFormatter;
import models.geo.GeoJsonPoint;
import org.bson.types.ObjectId;
import play.libs.Json;

import java.util.*;

/**
 * 旅行派的formatter基类
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public abstract class TaoziBaseFormatter implements JsonFormatter {

    protected Set<String> stringFields;

    protected Set<String> listFields;

    protected Set<String> mapFields;

    protected Set<String> filteredFields;

    protected TaoziBaseFormatter() {
        stringFields = new HashSet<>();
        listFields = new HashSet<>();
        mapFields = new HashSet<>();
        filteredFields = new HashSet<>();
    }

    protected ObjectMapper getObjectMapper() {
        return getObjectMapper(null, null);
    }

    protected <T> ObjectMapper getObjectMapper(Map<String, PropertyFilter> filterMap,
                                               Map<Class<? extends T>, JsonSerializer<T>> serializerMap) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        if (filterMap == null)
            filterMap = new HashMap<>();

        if (serializerMap == null)
            serializerMap = new HashMap<>();

        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        for (Map.Entry<Class<? extends T>, JsonSerializer<T>> entry : serializerMap.entrySet())
            module.addSerializer(entry.getKey(), entry.getValue());
        mapper.registerModule(module);

        SimpleFilterProvider filters = new SimpleFilterProvider()
                .addFilter("geoJsonPointFilter", SimpleBeanPropertyFilter.filterOutAllExcept(GeoJsonPoint.FD_COORDS));
        for (Map.Entry<String, PropertyFilter> entry : filterMap.entrySet())
            filters.addFilter(entry.getKey(), entry.getValue());
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
