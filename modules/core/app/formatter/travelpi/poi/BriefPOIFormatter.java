package formatter.travelpi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouBeanPropertyFilter;
import formatter.travelpi.ImageItemPlainSerializer;
import formatter.travelpi.TravelPiBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import play.libs.Json;

import java.util.*;

/**
 * @author Zephyre
 */
public class BriefPOIFormatter extends TravelPiBaseFormatter {

    private static BriefPOIFormatter instance;

    private BriefPOIFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_DESC, ViewSpot.FD_TIME_COST_DESC));

        listFields = new HashSet<>();
        listFields.addAll(Arrays.asList(AbstractPOI.FD_IMAGES, AbstractPOI.FD_TAGS));
    }

    public synchronized static BriefPOIFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new BriefPOIFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = new ObjectMapper();

        AbstractPOI vsItem = (AbstractPOI) item;

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

        FilterProvider filters = new SimpleFilterProvider().addFilter("abstractPOIFilter", theFilter);
        mapper.setFilters(filters);

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class, new ImageItemPlainSerializer());
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);
        result.put("_id", result.get("id").asText());
        result.put("name", result.get("zhName").asText());
        result.put("fullName", result.get("zhName").asText());

//        List<ImageItem> images = vsItem.getImages();
////        JsonNode images = result.get(Locality.fnImages);
//        result.remove(Locality.fnImages);
//        List<String> imageList = new ArrayList<>();
//        int idx = 0;
//        // 最大宽度800
//        int maxWidth = 800;
//        for (ImageItem img : images) {
//            Map<String, Integer> cropHint = img.getCropHint();
//            String url;
//            if (cropHint == null) {
//                url = String.format("http://lvxingpai-img-store.qiniudn.com/%s?imageView2/2/w/%d", img.getKey(),
//                        maxWidth);
//            } else {
//                int top = cropHint.get("top");
//                int right = cropHint.get("right");
//                int bottom = cropHint.get("bottom");
//                int left = cropHint.get("left");
//
//                url = String.format("http://lvxingpai-img-store.qiniudn.com/%s?imageMogr2/auto-orient/strip/gravity" +
//                                "/NorthWest/crop/!%dx%da%da%d/thumbnail/%dx",
//                        img.getKey(), (right - left), (bottom - top), left, top, maxWidth);
//            }
//
//            imageList.add(url);
//            idx++;
//            if (idx > 5)
//                break;
//        }
//        result.put("imageList", Json.toJson(imageList));
//        result.remove(Locality.fnImages);

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

        Map<String, Double> ratings = new HashMap<>();
        Double r = vsItem.getRating();
        if (r == null || r == 0)
            r = (new Random().nextDouble()) * 0.2 + 0.5;
        ratings.put("ranking", r);
        result.put("ratings", Json.toJson(ratings));

        if (vsItem instanceof ViewSpot) {
            Double t = ((ViewSpot) vsItem).getTimeCost();
            if (t == null || t == 0)
                t = (double) (new Random().nextInt(4) + 1);
            result.put("timeCost", t);
        }

        String addrText = vsItem.getAddress();
        if (addrText == null)
            addrText = "";

        String telText = vsItem.getTelephone();

        if (result.get("addr") == null) {
            Map<String, Object> addr = new HashMap<>();
            addr.put("addr", addrText);
            result.put("addr", Json.toJson(addr));
        } else if (result.get("addr").get("addr") == null) {
            ObjectNode tmp = (ObjectNode) result.get("addr");
            tmp.put("addr", addrText);
        }

        if (result.get("contact") == null) {
            Map<String, Object> addr = new HashMap<>();
            addr.put("phoneList", new ArrayList<>());
            result.put("contact", Json.toJson(addr));
        } else if (result.get("contact").get("phoneList") == null) {
            ObjectNode tmp = (ObjectNode) result.get("contact");
            if (telText == null)
                tmp.put("phoneList", Json.toJson(new ArrayList()));
            else
                tmp.put("phoneList", Json.toJson(Arrays.asList(telText)));
        }

        return postProcess(result);
    }
}
