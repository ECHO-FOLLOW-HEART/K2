package utils.formatter.travelpi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.geo.Destination;
import models.geo.GeoJsonPoint;
import play.libs.Json;
import utils.formatter.travelpi.TravelPiBaseFormatter;

import java.util.*;

/**
 * 目的地formatter
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public class DestinationFormatter extends TravelPiBaseFormatter {

    public DestinationFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(Destination.fnEnName, Destination.fnZhName));

        listFields = new HashSet<>();
        listFields.addAll(Arrays.asList(Destination.fnTags, Destination.fnImages));
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();
        Destination destItem = (Destination) item;

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
                includedFields.add(Destination.fnEnName);
                includedFields.add(Destination.fnZhName);
                includedFields.add(Destination.fnDesc);
                includedFields.add(Destination.fnRating);
                includedFields.add(Destination.fnHotness);
                includedFields.add(Destination.fnTags);
                includedFields.add(Destination.fnAbroad);
                includedFields.add("id");

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

        ObjectNode result = postProcess((ObjectNode) mapper.valueToTree(destItem));

        String name;
        try {
            name = result.get(Destination.fnZhName).asText();
        } catch (NullPointerException e) {
            name = "";
        }
        result.put("name", name);
        result.put("fullName", name);

        GeoJsonPoint location = destItem.getLocation();
        if (location != null) {
            double lng = location.getCoordinates()[0];
            double lat = location.getCoordinates()[1];
            result.put("lat", lat);
            result.put("lng", lng);
        }

        JsonNode images = result.get(Destination.fnImages);
        result.remove(Destination.fnImages);
        List<String> imageList = new ArrayList<>();
        for (JsonNode img : images) {
            String url = img.get("url").asText();
            imageList.add(url + "?imageView2/2/w/800");
        }
        result.put("imageList", Json.toJson(imageList));

        return result;
    }
}
