package formatter.taozi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class SimplePOIFormatter extends TaoziBaseFormatter {

    public SimplePOIFormatter setImageWidth(int width) {
        imageWidth = width;
        return this;
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_ZH_NAME,
                AbstractPOI.FD_DESC,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_ADDRESS
        );
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("abstractPOIFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(imageWidth));
        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        ObjectNode result = mapper.valueToTree(item);

        mapFields.add(AbstractPOI.FD_LOCATION);

        return postProcess(result);
    }
}
