package formatter.taozi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;

import java.util.*;

/**
 * Created by lxf on 14-11-1.
 */
public class DestinationPOIFormatter extends TaoziBaseFormatter {
    public static final Collection<String> retrievedFields;

    static {
        retrievedFields = Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnDesc,
                Locality.fnImages);
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class, new ImageItemSerializer());
        mapper.registerModule(imageItemModule);

        Set<String> filterSet = new HashSet<>();
        Collections.addAll(filterSet, retrievedFields.toArray(new String[retrievedFields.size()]));

        FilterProvider filters = new SimpleFilterProvider().addFilter("destinationFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(filterSet));
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
//
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
//
//        PropertyFilter theFilter = new SimpleBeanPropertyFilter() {
//            @Override
//            public void serializeAsField
//                    (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
//                if (include(writer)) {
//                    writer.serializeAsField(pojo, jgen, provider);
//                } else if (!jgen.canOmitFields()) { // since 2.3
//                    writer.serializeAsOmittedField(pojo, jgen, provider);
//                }
//            }
//
//            private boolean includeImpl(PropertyWriter writer) {
//                Set<String> includedFields = new HashSet<>();
//                Collections.addAll(includedFields, "id", Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnDesc,
//                        Locality.fnImages);
//
//                return (includedFields.contains(writer.getName()));
//            }
//
//            @Override
//            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
//                return includeImpl(beanPropertyWriter);
//            }
//
//            @Override
//            protected boolean include(PropertyWriter writer) {
//                return includeImpl(writer);
//            }
//        };
//        PropertyFilter coordsFilter = new SimpleBeanPropertyFilter() {
//            @Override
//            public void serializeAsField
//                    (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
//                if (include(writer)) {
//                    writer.serializeAsField(pojo, jgen, provider);
//                } else if (!jgen.canOmitFields()) { // since 2.3
//                    writer.serializeAsOmittedField(pojo, jgen, provider);
//                }
//            }
//
//            private boolean includeImpl(PropertyWriter writer) {
//                Set<String> includedFields = new HashSet<>();
//                includedFields.add(Coords.simpLat);
//                includedFields.add(Coords.simpLng);
//                return (includedFields.contains(writer.getName()));
//            }
//
//            @Override
//            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
//                return includeImpl(beanPropertyWriter);
//            }
//
//            @Override
//            protected boolean include(PropertyWriter writer) {
//                return includeImpl(writer);
//            }
//        };
//
//        PropertyFilter imgFilter = new SimpleBeanPropertyFilter() {
//            @Override
//            public void serializeAsField
//                    (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
//                if (include(writer)) {
//                    writer.serializeAsField(pojo, jgen, provider);
//                } else if (!jgen.canOmitFields()) { // since 2.3
//                    writer.serializeAsOmittedField(pojo, jgen, provider);
//                }
//            }
//
//            private boolean includeImpl(PropertyWriter writer) {
//                Set<String> includedFields = new HashSet<>();
//                includedFields.add(ImageItem.FD_URL);
//                return (includedFields.contains(writer.getName()));
//            }
//
//            @Override
//            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
//                return includeImpl(beanPropertyWriter);
//            }
//
//            @Override
//            protected boolean include(PropertyWriter writer) {
//                return includeImpl(writer);
//            }
//        };
//        FilterProvider filters = new SimpleFilterProvider().addFilter("destinationFilter", theFilter).addFilter("coordsFilter", coordsFilter).addFilter("imageItemPOIFilter", imgFilter);
//        mapper.setFilters(filters);
//
//        return mapper.valueToTree(item);
    }
}
