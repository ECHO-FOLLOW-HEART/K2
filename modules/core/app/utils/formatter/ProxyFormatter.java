package utils.formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * 获得代理服务器信息
 * <p/>
 * Created by zephyre on 10/29/14.
 */
public class ProxyFormatter implements JsonFormatter {
    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsField
                    (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
                if (include(writer)) {
                    writer.serializeAsField(pojo, jgen, provider);
                } else if (!jgen.canOmitFields()) { // since 2.3
                    writer.serializeAsOmittedField(pojo, jgen, provider);
                }
            }

            private boolean excludeImpl(PropertyWriter writer) {
                Set<String> excludedFields = new HashSet<>();
                excludedFields.add("id");

                return (excludedFields.contains(writer.getName()));
            }

            @Override
            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
                return !excludeImpl(beanPropertyWriter);
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return !excludeImpl(writer);
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("proxyFilter", theFilter);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ"));
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }


}
