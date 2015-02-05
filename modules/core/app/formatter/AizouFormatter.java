package formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ObjectIdSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.misc.ImageItem;
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

    public JsonNode formatNode(List<T> itemList) throws JsonProcessingException {
        return mapper.valueToTree(itemList);
    }

    public JsonNode formatNode(T item) throws JsonProcessingException {
        return mapper.valueToTree(item);
    }

    protected ObjectMapper mapper;

    protected Set<String> filteredFields = new HashSet<>();

    protected SimpleModule module = new SimpleModule();

    protected int imageWidth;


    public Set<String> getFilteredFields() {
        return filteredFields;
    }

    public <T2> void registerSerializer(Class<? extends T2> cls, JsonSerializer<T2> serializer){
        module.addSerializer(cls, serializer);
    }

    protected ObjectMapper initObjectMapper(Map<String, PropertyFilter> filterMap) {
        mapper = new ObjectMapper();

        if (filterMap == null)
            filterMap = new HashMap<>();

        // 添加ObjectId的序列化
        registerSerializer(ObjectId.class, new ObjectIdSerializer());

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
