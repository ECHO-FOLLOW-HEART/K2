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
public class DetailsEntyrFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("localityFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        Locality.fnRemoteTraffic,
                        Locality.fnLocalTraffic,
                        Locality.fnActivityIntro,
                        Locality.fnActivities,
                        Locality.fnTips,
                        Locality.fnShoppingIntro,
                        Locality.fnCommodities,
                        Locality.fnDinningIntro,
                        Locality.fnCuisines,
                        Locality.fnDesc,
                        Locality.fnGeoHistory,
                        Locality.fnSpecials));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}
