package formatter.taozi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;

import java.util.*;

/**
 * Created by lxf on 14-11-1.
 */
public class LocalityGuideFormatter extends TaoziBaseFormatter {
    @Override
    public JsonNode format(AizouBaseEntity item) {
        return null;
    }

    public JsonNode format(AizouBaseEntity item, String kind) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        Set<String> set = new HashSet<>();
        if (kind.equals("remote")) {
            Collections.addAll(set, Locality.fnRemoteTraffic);
        }
        if (kind.equals("local")) {
            Collections.addAll(set, Locality.fnLocalTraffic);
        }
        if (kind.equals("activity")) {
            Collections.addAll(set, Locality.fnActivityIntro, Locality.fnActivities);
        }
        if (kind.equals("tips")) {
            Collections.addAll(set, Locality.fnTips);
        }
        if (kind.equals("shopping")) {
            Collections.addAll(set, Locality.fnShoppingIntro, Locality.fnCommodities);
        }
        if (kind.equals("dinning")) {
            Collections.addAll(set, Locality.fnDinningIntro, Locality.fnCuisines);
        }
        if (kind.equals("desc")) {
            Collections.addAll(set, Locality.fnDesc);
        }
        filterMap.put("localityFilter", SimpleBeanPropertyFilter.filterOutAllExcept(set));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}
