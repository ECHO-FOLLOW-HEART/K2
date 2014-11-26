package utils.formatter.taozi.guide;

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
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.poi.AbstractPOI;
import utils.formatter.JsonFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * 返回攻略中行程单内容
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class ShoppingFormatter implements JsonFormatter {
    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter guideFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(AbstractGuide.fdId);
                includedFields.add(Guide.fnUserId);
                includedFields.add(AbstractGuide.fnTitle);
                // 购物
                includedFields.add(AbstractGuide.fnShopping);
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
        PropertyFilter poiFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(AbstractPOI.simpDesc);
                includedFields.add(AbstractPOI.simpID);
                includedFields.add(AbstractPOI.simpZhName);
                includedFields.add(AbstractPOI.simpEnName);
                includedFields.add(AbstractPOI.simpImg);
                includedFields.add(AbstractPOI.simpRating);
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

        FilterProvider filters = new SimpleFilterProvider().addFilter("guideFilter", guideFilter).addFilter("abstractPOIFilter", poiFilter);
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }
}
