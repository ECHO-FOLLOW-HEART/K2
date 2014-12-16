package formatter.taozi.guide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.guide.AbstractGuide;
import models.guide.DestGuideInfo;
import models.misc.ImageItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class DestGuideFormatter extends TaoziBaseFormatter {

    public DestGuideFormatter(String type) {

        if (type.equals(AbstractGuide.fnShopping)) {
            filteredFields = new HashSet<>();
            Collections.addAll(filteredFields,
                    DestGuideInfo.fnShoppingImages,
                    DestGuideInfo.fnShoppingDesc);
        } else if (type.equals(AbstractGuide.fnRestaurant)) {
            filteredFields = new HashSet<>();
            Collections.addAll(filteredFields,
                    DestGuideInfo.fnShoppingImages,
                    DestGuideInfo.fnShoppingDesc);
        }
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("destGuideInfoFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        return mapper.valueToTree(item);
    }
}

