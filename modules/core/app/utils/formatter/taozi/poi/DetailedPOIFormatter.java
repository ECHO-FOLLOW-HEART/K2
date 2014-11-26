package utils.formatter.taozi.poi;

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
import models.geo.Country;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import utils.formatter.JsonFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class DetailedPOIFormatter implements JsonFormatter {

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        return null;
    }

    public JsonNode format(TravelPiBaseItem item, final String poiType) {
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
                includedFields.add(AbstractPOI.simpID);
                includedFields.add(AbstractPOI.simpZhName);
                includedFields.add(AbstractPOI.simpEnName);
                includedFields.add(AbstractPOI.simpDesc);
                includedFields.add(AbstractPOI.simpImg);
                includedFields.add(AbstractPOI.fnLocation);
                includedFields.add(AbstractPOI.detAlias);
                includedFields.add(AbstractPOI.simpRating);

                includedFields.add(AbstractPOI.simpAddress);
                includedFields.add(AbstractPOI.detPriceDesc);
                includedFields.add(AbstractPOI.simpTelephone);
                //includedFields.add(AbstractPOI.simpCountry);
                //includedFields.add(AbstractPOI.simplocList);

                if (poiType.equals("vs")) {
                    includedFields.add(ViewSpot.fnOpenTime);
                    includedFields.add(ViewSpot.fnTimeCostDesc);
                    includedFields.add(ViewSpot.fnTravelMonth);
                    includedFields.add(ViewSpot.detGuideInfoUrl);
                    includedFields.add(ViewSpot.detTrafficInfoUrl);
                    includedFields.add(ViewSpot.detKengDieInfoUrl);
                } else if (poiType.equals("restaurant") || poiType.equals("shopping")) {
                    includedFields.add(AbstractPOI.fnMoreCommentsUrl);
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

        PropertyFilter geoJsonPointFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(GeoJsonPoint.fnCoordinates);
                includedFields.add(GeoJsonPoint.fnType);
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

        PropertyFilter imgFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(ImageItem.fnUrl);
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

        PropertyFilter localityFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(Locality.fnZhName);
                includedFields.add(Locality.fnEnName);
                includedFields.add(Locality.fnId);
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

        PropertyFilter countryFilter = new SimpleBeanPropertyFilter() {
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
                includedFields.add(Country.fnId);
                includedFields.add(Country.fnZhName);
                includedFields.add(Country.fnEnName);
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

        FilterProvider filters = new SimpleFilterProvider().addFilter("abstractPOIFilter", theFilter)
                .addFilter("geoJsonPointFilter", geoJsonPointFilter).addFilter("imageItemPOIFilter", imgFilter)
                .addFilter("localityFilter", localityFilter).addFilter("countryFilter", countryFilter);
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }

}
