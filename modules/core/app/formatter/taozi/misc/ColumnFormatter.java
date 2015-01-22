package formatter.taozi.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.Column;
import models.misc.ImageItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by zephyre on 12/11/14.
 */
public class ColumnFormatter extends TaoziBaseFormatter {

    public ColumnFormatter() {
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                Column.FD_CONTENT,
                Column.FD_COVER,
                Column.FD_LINK,
                Column.FD_TITLE,
                Column.FD_TYPE,
                Column.FD_ID
        );
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("columnFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(ImageItemSerializerOld.ImageSizeDesc.MEDIUM));
        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}
