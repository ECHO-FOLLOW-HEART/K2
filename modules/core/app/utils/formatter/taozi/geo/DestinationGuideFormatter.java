package utils.formatter.taozi.geo;

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
import models.geo.Destination;
import sun.security.krb5.internal.crypto.Des;
import utils.formatter.JsonFormatter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lxf on 14-11-1.
 */
public class DestinationGuideFormatter implements JsonFormatter {
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

            private boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, Destination.fnActivities, Destination.fnActivityIntro
                        , Destination.fnRemoteTraffic, Destination.fnLocalTraffic, Destination.fnTips,
                        Destination.fnShoppingIntro, Destination.fnCommodities, Destination.fnDinningIntro, Destination.fnCuisines);

                return (includedFields.contains(writer.getName()));
            }

            @Override
            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
                return includeImpl(beanPropertyWriter);
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return includeImpl(writer);
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("destinationFilter", theFilter);
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }

    public JsonNode format(TravelPiBaseItem item, final String kind) {
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

            private boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                if (kind.equals("remote")) {
                    Collections.addAll(includedFields, Destination.fnRemoteTraffic);
                }
                if (kind.equals("local")) {
                    Collections.addAll(includedFields, Destination.fnLocalTraffic);
                }
                if (kind.equals("activity")) {
                    Collections.addAll(includedFields, Destination.fnActivityIntro, Destination.fnActivities);
                }
                if (kind.equals("tips")) {
                    Collections.addAll(includedFields, Destination.fnTips);
                }
                if (kind.equals("shopping")) {
                    Collections.addAll(includedFields, Destination.fnShoppingIntro, Destination.fnCommodities);
                }
                if (kind.equals("dinning")) {
                    Collections.addAll(includedFields, Destination.fnDinningIntro, Destination.fnCuisines);
                }
                return (includedFields.contains(writer.getName()));
            }

            @Override
            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
                return includeImpl(beanPropertyWriter);
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return includeImpl(writer);
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("destinationFilter", theFilter);
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }
}
