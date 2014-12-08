package formatter.travelpi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.travelpi.ImageItemPlainSerializer;
import models.TravelPiBaseItem;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import play.libs.Json;
import formatter.AizouBeanPropertyFilter;
import formatter.travelpi.TravelPiBaseFormatter;

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
        stringFields.addAll(Arrays.asList(Locality.fnEnName, Locality.fnZhName));

        listFields = new HashSet<>();
        listFields.addAll(Arrays.asList(Locality.fnTags, Locality.fnImages, "relVs"));
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
        Locality destItem = (Locality) item;

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class, new ImageItemPlainSerializer());
        mapper.registerModule(imageItemModule);

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, "id", Locality.fnEnName, Locality.fnZhName, Locality.fnDesc,
                        Locality.fnRating, Locality.fnHotness, Locality.fnTags, Locality.fnAbroad,
                        Locality.fnImages);

                return (includedFields.contains(writer.getName()));
            }
        };

        PropertyFilter imageFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, ImageItem.FD_URL, ImageItem.FD_CROP_HINT, ImageItem.FD_WIDTH,
                        ImageItem.FD_HEIGHT);

                return (includedFields.contains(writer.getName()));
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("localityFilter", theFilter)
                .addFilter("imageItemFilter", imageFilter);
        mapper.setFilters(filters);

        ObjectNode result = postProcess((ObjectNode) mapper.valueToTree(destItem));

        String name;
        try {
            name = result.get(Locality.fnZhName).asText();
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

        ObjectNode ratings = Json.newObject();
        Double r = destItem.getRating();
        if (r == null)
            r = destItem.getHotness() * 0.88 + (new Random().nextDouble() - 0.5) * 0.15;
        if (r == null)
            r = 0.6;
        else if (r > 1)
            r = 1.0;
        ratings.put("ranking", r.doubleValue());
        result.put("ratings", ratings);

//        JsonNode images = result.get(Locality.fnImages);
//        result.remove(Locality.fnImages);
//        List<String> imageList = new ArrayList<>();
//        int idx = 0;
//        // 最大宽度800
//        int maxWidth = 800;
//        for (JsonNode img : images) {
//            JsonNode cropHint = img.get(ImageItem.FD_CROP_HINT);
//            String url;
//            if (cropHint == null || cropHint.isNull()) {
//                url = String.format("%s?imageView2/2/w/%d", img.get("url").asText(), maxWidth);
//            } else {
//                int top = cropHint.get("top").asInt();
//                int right = cropHint.get("right").asInt();
//                int bottom = cropHint.get("bottom").asInt();
//                int left = cropHint.get("left").asInt();
//
//                url = String.format("%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!%dx%da%da%d/thumbnail/%dx",
//                        img.get("url").asText(), (right - left), (bottom - top), left, top, maxWidth);
//            }
//
//            imageList.add(url);
//            idx++;
//            if (idx > 5)
//                break;
//        }
//        result.put("imageList", Json.toJson(imageList));

        return result;
    }
}
