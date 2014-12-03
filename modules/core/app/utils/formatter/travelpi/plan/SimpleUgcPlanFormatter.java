package utils.formatter.travelpi.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import models.plan.AbstractPlan;
import models.plan.UgcPlan;
import play.libs.Json;
import utils.formatter.AizouBeanPropertyFilter;
import utils.formatter.travelpi.TravelPiBaseFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户自定义路线的formatter
 *
 * @author Zephyre
 */
public class SimpleUgcPlanFormatter extends TravelPiBaseFormatter {
    private static SimpleUgcPlanFormatter instance;

    private SimpleUgcPlanFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(UgcPlan.FD_START_DATE, UgcPlan.FD_END_DATE));

        listFields = new HashSet<>();
    }

    public synchronized static SimpleUgcPlanFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new SimpleUgcPlanFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();
        UgcPlan destItem = (UgcPlan) item;

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, "id", UgcPlan.FD_START_DATE, UgcPlan.FD_END_DATE,
                        UgcPlan.FD_UPDATE_TIME, AbstractPlan.FD_TITLE, AbstractPlan.FD_DAYS, AbstractPlan.FD_IMAGES);

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

        FilterProvider filters = new SimpleFilterProvider().addFilter("ugcPlanFilter", theFilter)
                .addFilter("imageItemFilter", imageFilter);
        mapper.setFilters(filters);

        ObjectNode result = mapper.valueToTree(destItem);


        JsonNode images = result.get(AbstractPlan.FD_IMAGES);
        result.remove(UgcPlan.FD_IMAGES);
        List<String> imageList = new ArrayList<>();
        int idx = 0;
        // 最大宽度800
        int maxWidth = 800;
        for (JsonNode img : images) {
            JsonNode cropHint = img.get(ImageItem.FD_CROP_HINT);
            String url;
            if (cropHint == null || cropHint.isNull()) {
                url = String.format("%s?imageView2/2/w/%d", img.get("url").asText(), maxWidth);
            } else {
                int top = cropHint.get("top").asInt();
                int right = cropHint.get("right").asInt();
                int bottom = cropHint.get("bottom").asInt();
                int left = cropHint.get("left").asInt();

                url = String.format("%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!%dx%da%da%d/thumbnail/%dx",
                        img.get("url").asText(), (right - left), (bottom - top), left, top, maxWidth);
            }

            imageList.add(url);
            idx++;
            if (idx > 5)
                break;
        }
        result.put("imageList", Json.toJson(imageList));

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        for (String key : new String[]{UgcPlan.FD_START_DATE, UgcPlan.FD_END_DATE}) {
            long ts = result.get(key).asLong();
            Date date = new Date();
            date.setTime(ts);
            result.put(key, fmt.format(date));
        }

        return postProcess(result);
    }
}
