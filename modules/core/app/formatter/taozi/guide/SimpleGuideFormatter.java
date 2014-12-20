package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import formatter.JsonFormatter;
import models.poi.AbstractPOI;
import models.traffic.AbstractTrafficHub;

import java.util.*;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class SimpleGuideFormatter extends TaoziBaseFormatter {

    public SimpleGuideFormatter(){
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

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers(filteredFields);

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("guideFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        return mapper.valueToTree(item);
    }
}