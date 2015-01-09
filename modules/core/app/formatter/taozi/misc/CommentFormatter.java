package formatter.taozi.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.Comment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zephyre on 12/11/14.
 */
public class CommentFormatter extends TaoziBaseFormatter {
    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("commentFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        Comment.FD_AVATAR,
                        Comment.FD_AUTHOR_NAME,
                        Comment.FD_USER_ID,
                        Comment.FD_RATING,
                        Comment.FD_CONTENTS,
                        Comment.FD_CTIME,
                        Comment.FD_IMAGES
                ));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}
