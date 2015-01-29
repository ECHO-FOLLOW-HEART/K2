package formatter.taozi.TravelNote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.misc.TravelNote;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lxf on 14-11-1.
 */
public class DetailTravelNoteFormatter extends TaoziBaseFormatter {

    public DetailTravelNoteFormatter setImageWidth(int width) {
        imageWidth = width;
        return this;
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("travelNoteFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        TravelNote.fnAuthorName,
                        TravelNote.fnAuthorAvatar,
                        TravelNote.fnImages,
                        TravelNote.fnTitle,
                        TravelNote.fnPublishTime,
                        TravelNote.fnRating,
                        TravelNote.fnTravelTime,
                        TravelNote.fnNoteContents,
                        TravelNote.fnUpperCost,
                        TravelNote.fnLowerCost,
                        TravelNote.fnCommentCnt,
                        TravelNote.fnViewCnt,
                        TravelNote.fnFavorCnt
                ));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(imageWidth));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}
