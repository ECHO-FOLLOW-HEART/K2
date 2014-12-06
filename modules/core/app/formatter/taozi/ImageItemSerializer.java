package formatter.taozi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.ImageItem;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class ImageItemSerializer extends JsonSerializer<ImageItem> {
    @Override
    public void serialize(ImageItem imageItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("url", imageItem.getUrl());
        jsonGenerator.writeEndObject();
    }
}
