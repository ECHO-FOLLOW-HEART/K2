package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.Comment;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zephyre on 12/11/14.
 */
public class CommentFormatter extends AizouFormatter<Comment> {

    private ImageItemSerializer imageItemSerializer;

    public CommentFormatter() {
        registerSerializer(Comment.class, new CommentSerializer());

        imageItemSerializer = new ImageItemSerializer();
        registerSerializer(ImageItem.class, imageItemSerializer);

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID,
                Comment.FD_AVATAR,
                Comment.FD_AUTHOR_NAME,
                Comment.FD_USER_ID,
                Comment.FD_RATING,
                Comment.FD_CONTENTS,
                Comment.FD_PUBLISHTIME,
                Comment.FD_IMAGES));
    }

    class CommentSerializer extends AizouSerializer<Comment> {

        @Override
        public void serialize(Comment comment, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {
            jgen.writeStartObject();

            writeObjectId(comment, jgen, serializerProvider);

            jgen.writeFieldName(Comment.FD_IMAGES);
            List<ImageItem> images = comment.getImages();
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            if (comment.getUserId() == null)
                jgen.writeNullField(Comment.FD_USER_ID);
            else
                jgen.writeNumberField(Comment.FD_USER_ID, getValue(comment.getUserId()));

            jgen.writeStringField(Comment.FD_AVATAR, getString(comment.getAuthorAvatar()));
            jgen.writeStringField(Comment.FD_AUTHOR_NAME, getString(comment.getAuthorName()));
            jgen.writeStringField(Comment.FD_CONTENTS, getString(comment.getContents()));
            jgen.writeObjectField(Comment.FD_RATING, getValue(comment.getRating()));
            jgen.writeObjectField(Comment.FD_PUBLISHTIME, getValue(comment.getPublishTime()));

            jgen.writeEndObject();
        }
    }

}
