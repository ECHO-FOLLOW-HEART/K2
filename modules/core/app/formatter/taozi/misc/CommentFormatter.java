package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.Column;
import models.misc.ImageItem;
import models.poi.Comment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zephyre on 12/11/14.
 */
public class CommentFormatter extends AizouFormatter<Comment> {

    public CommentFormatter() {
        registerSerializer(Comment.class, new CommentSerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer());
        initObjectMapper(null);
    }

    //    @Override
//    public JsonNode format(AizouBaseEntity item) {
//
//        item.fillNullMembers();
//
//        Map<String, PropertyFilter> filterMap = new HashMap<>();
//        filterMap.put("commentFilter",
//                SimpleBeanPropertyFilter.filterOutAllExcept(
//                        Comment.FD_AVATAR,
//                        Comment.FD_AUTHOR_NAME,
//                        Comment.FD_USER_ID,
//                        Comment.FD_RATING,
//                        Comment.FD_CONTENTS,
//                        Comment.FD_PUBLISHTIME,
//                        Comment.FD_IMAGES
//                ));
//
//        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
//        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(ImageItemSerializerOld.ImageSizeDesc.MEDIUM));
//
//        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);
//
//        return mapper.valueToTree(item);
//    }
    class CommentSerializer extends AizouSerializer<Comment> {

        @Override
        public void serialize(Comment comment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeStringField(Comment.FD_AVATAR, getString(comment.getAuthorAvatar()));
            jsonGenerator.writeStringField(Comment.FD_AUTHOR_NAME, getString(comment.getAuthorName()));

            if (comment.getUserId() == null)
                jsonGenerator.writeNullField(Comment.FD_USER_ID);
            else
                jsonGenerator.writeNumberField(Comment.FD_USER_ID, getValue(comment.getUserId()));
            jsonGenerator.writeNumberField(Comment.FD_RATING, getValue(comment.getRating()));

            jsonGenerator.writeStringField(Comment.FD_CONTENTS, getString(comment.getContents()));
            jsonGenerator.writeNumberField(Comment.FD_PUBLISHTIME, getValue(comment.getPublishTime()));
            // Images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = comment.getImages();
            jsonGenerator.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jsonGenerator, serializerProvider);
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeEndObject();
        }
    }
}
