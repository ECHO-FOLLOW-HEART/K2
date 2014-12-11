package formatter.taozi.poi;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Comment;

import java.util.*;

/**
 * 返回POI的推荐
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class CommentFormatter extends TaoziBaseFormatter {

    public CommentFormatter(){
        stringFields.addAll(Arrays.asList(
                Comment.FD_AVATAR,
                Comment.FD_NICK_NAME,
                Comment.FD_CONTENTS
        ));

        listFields.add(AbstractPOI.FD_IMAGES);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                Comment.FD_USER_ID,
                Comment.FD_AVATAR,
                Comment.FD_NICK_NAME,
                Comment.FD_RATING,
                Comment.FD_CONTENTS,
                Comment.FD_TIME,
                Comment.FD_IMAGS);
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        Map<String, PropertyFilter> filterMap=new HashMap<>();
        filterMap.put("commentFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap =new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return postProcess((ObjectNode) mapper.valueToTree(item));
    }
}
