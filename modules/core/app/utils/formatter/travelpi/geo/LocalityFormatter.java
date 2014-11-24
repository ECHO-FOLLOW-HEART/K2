package utils.formatter.travelpi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.geo.Destination;
import models.geo.GeoJsonPoint;
import play.libs.Json;
import utils.formatter.AizouBeanPropertyFilter;
import utils.formatter.travelpi.TravelPiBaseFormatter;

import java.util.*;

/**
 * 目的地formatter
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public class LocalityFormatter extends TravelPiBaseFormatter {

    private static LocalityFormatter instance;

    private LocalityFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(Destination.fnEnName, Destination.fnZhName));

        listFields = new HashSet<>();
        listFields.addAll(Arrays.asList(Destination.fnTags, Destination.fnImages));
    }

    public synchronized static LocalityFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new LocalityFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();
        Destination destItem = (Destination) item;

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
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
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("localityFilter", theFilter);
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
