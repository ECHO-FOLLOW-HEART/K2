package formatter.web.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.guide.DestGuideInfo;
import models.misc.ImageItem;
import models.misc.Recommendation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by topy on 2014/12/26.
 */
public class RecommendationFormatter extends TaoziBaseFormatter {

    public RecommendationFormatter() {

            filteredFields = new HashSet<>();
            Collections.addAll(filteredFields,
                    Recommendation.FD_ID,
                    Recommendation.FD_NAME,
                    Recommendation.FD_IMAGES);

    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("recommendationFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        return mapper.valueToTree(item);
    }
}