package formatter.taozi.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.Answer;
import models.misc.ImageItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lxf on 15-1-26.
 */
public class QaFormatter extends TaoziBaseFormatter {
    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("qaFormatter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        //Answer.fnSource,
                        Answer.fnAuthorAvatar,
                        Answer.fnAuthorName,
                        Answer.fnPublishTime,
                        Answer.fnEssence,
                        Answer.fnContents
                ));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}

