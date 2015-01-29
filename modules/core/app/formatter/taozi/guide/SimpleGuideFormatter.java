package formatter.taozi.guide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class SimpleGuideFormatter extends TaoziBaseFormatter {

    public SimpleGuideFormatter() {
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AbstractGuide.fdId,
                AbstractGuide.fnTitle,
                Guide.fnUpdateTime,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING
        );
    }

    public SimpleGuideFormatter setImageWidth(int width) {
        imageWidth = width;
        return this;
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers(filteredFields);

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("guideFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializerOld(imageWidth));
        mapper.registerModule(imageItemModule);

        return mapper.valueToTree(item);
    }
}