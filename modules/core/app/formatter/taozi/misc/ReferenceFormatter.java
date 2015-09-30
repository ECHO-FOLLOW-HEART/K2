package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.misc.ImageItem;
import models.misc.Reference;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;

/**
 * 推荐给用户的项目
 * <p>
 * Created by topy
 */
public class ReferenceFormatter extends AizouFormatter<Reference> {

    public ReferenceFormatter() {

        registerSerializer(Reference.class, new ReferenceSerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(640));
        initObjectMapper(null);

    }

    class ReferenceSerializer extends AizouSerializer<Reference> {

        @Override
        public void serialize(Reference simpleRef, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();

            //writeObjectId(simpleRef, jsonGenerator, serializerProvider);
            ObjectId itemId = simpleRef.getItemId();
            jsonGenerator.writeStringField("id", getString(itemId == null ? "" : itemId.toString()));
            jsonGenerator.writeStringField(Reference.FD_ZH_NAME, getString(simpleRef.getZhName()));
            jsonGenerator.writeStringField(Reference.FD_EN_NAME, getString(simpleRef.getEnName()));

            // Images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = simpleRef.getImages();
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
