package formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ObjectIdSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by zephyre on 1/20/15.
 */
public abstract class AizouFormatter<T extends AizouBaseEntity> {

    public String format(List<T> itemList) throws JsonProcessingException {
        return mapper.writeValueAsString(itemList);
    }

    public String format(T item) throws JsonProcessingException {
        return mapper.writeValueAsString(item);
    }

    protected ObjectMapper mapper;

    protected Set<String> filteredFields = new HashSet<>();

    public Set<String> getFilteredFields() {
        return filteredFields;
    }

    protected static Map<Class, AizouFormatter> instanceMap = new Hashtable<>();

    protected ObjectMapper initObjectMapper(Map<String, PropertyFilter> filterMap,
                                            Map<Class<? extends T>, JsonSerializer<T>> serializerMap) {
        mapper = new ObjectMapper();

        if (filterMap == null)
            filterMap = new HashMap<>();

        if (serializerMap == null)
            serializerMap = new HashMap<>();

        SimpleModule module = new SimpleModule();

        // 添加ObjectId的序列化
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());

        for (Map.Entry<Class<? extends T>, JsonSerializer<T>> entry : serializerMap.entrySet()) {
            module.addSerializer(entry.getKey(), entry.getValue());
        }

        mapper.registerModule(module);

        // 添加Location的序列化
        SimpleFilterProvider filters = new SimpleFilterProvider()
                .addFilter("geoJsonPointFilter", SimpleBeanPropertyFilter.filterOutAllExcept(GeoJsonPoint.FD_COORDS));

        for (Map.Entry<String, PropertyFilter> entry : filterMap.entrySet())
            filters.addFilter(entry.getKey(), entry.getValue());

        mapper.setFilters(filters);

        return mapper;
    }
}
