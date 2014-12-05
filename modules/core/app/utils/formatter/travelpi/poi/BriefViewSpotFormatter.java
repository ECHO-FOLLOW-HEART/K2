package utils.formatter.travelpi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import play.libs.Json;
import utils.formatter.AizouBeanPropertyFilter;
import utils.formatter.travelpi.TravelPiBaseFormatter;

import java.util.*;

/**
 * @author Zephyre
 */
public class BriefViewSpotFormatter extends TravelPiBaseFormatter {

    private static BriefViewSpotFormatter instance;

    private BriefViewSpotFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_DESC, ViewSpot.FD_TIME_COST_DESC));

        listFields = new HashSet<>();
        listFields.addAll(Arrays.asList(AbstractPOI.FD_IMAGES, AbstractPOI.FD_TAGS));
    }

    public synchronized static BriefViewSpotFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new BriefViewSpotFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();

        ViewSpot vsItem = (ViewSpot) item;

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, "id", AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_NAME,
                        AbstractPOI.FD_DESC, ViewSpot.FD_TIME_COST_DESC, AbstractPOI.FD_TAGS, AbstractPOI.FD_IMAGES,
                        AbstractPOI.FD_PRICE, ViewSpot.FD_DESC_FLAGS);

                return (includedFields.contains(writer.getName()));
            }
        };

        PropertyFilter imageFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, ImageItem.fnUrl, ImageItem.FD_CROP_HINT, ImageItem.FD_WIDTH,
                        ImageItem.FD_HEIGHT);

                return (includedFields.contains(writer.getName()));
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("viewSpotFilter", theFilter)
                .addFilter("imageItemFilter", imageFilter);
        mapper.setFilters(filters);

        ObjectNode result = mapper.valueToTree(item);
        result.put("_id", result.get("id").asText());
//        result.put("name", result.get("zhName").asText());
        result.put("fullName", result.get("name").asText());

        List<ImageItem> images = vsItem.getImages();
//        JsonNode images = result.get(Locality.fnImages);
        result.remove(Locality.fnImages);
        List<String> imageList = new ArrayList<>();
        int idx = 0;
        // 最大宽度800
        int maxWidth = 800;
        for (ImageItem img : images) {
            Map<String, Integer> cropHint = img.getCropHint();
            String url;
            if (cropHint == null) {
                url = String.format("http://lvxingpai-img-store.qiniudn.com/%s?imageView2/2/w/%d", img.key, maxWidth);
            } else {
                int top = cropHint.get("top");
                int right = cropHint.get("right");
                int bottom = cropHint.get("bottom");
                int left = cropHint.get("left");

                url = String.format("http://lvxingpai-img-store.qiniudn.com/%s?imageMogr2/auto-orient/strip/gravity" +
                                "/NorthWest/crop/!%dx%da%da%d/thumbnail/%dx",
                        img.key, (right - left), (bottom - top), left, top, maxWidth);
            }

            imageList.add(url);
            idx++;
            if (idx > 5)
                break;
        }
        result.put("imageList", Json.toJson(imageList));
        result.remove(Locality.fnImages);

        Locality loc = vsItem.getLocality();
        ObjectNode locJson = Json.newObject();
        if (loc != null) {
            locJson.put("locName", loc.getZhName());
            locJson.put("locId", loc.getId().toString());
        }
        double[] coords = vsItem.getLocation().getCoordinates();
        locJson.put("lng", coords[0]);
        locJson.put("lat", coords[1]);

        result.put("addr", locJson);

        return postProcess(result);
    }
}
