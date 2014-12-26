package formatter.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.ImageItem;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class WebImageItemSerializer extends JsonSerializer<ImageItem> {

    public WebImageItemSerializer() {
    }

    public enum ImageSizeDesc {
        SMALL,
        MEDIUM,
        LARGE,
        FULL
    }

    @Override
    public void serialize(ImageItem imageItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();

        if (imageItem.getKey() == null && imageItem.getUrl() != null) {
            jsonGenerator.writeStringField("url", imageItem.getUrl());
            jsonGenerator.writeNumberField("width", null);
            jsonGenerator.writeNumberField("height", null);
            jsonGenerator.writeEndObject();
            return;
        }

        String fullUrl = imageItem.getFullUrl();

        int width = imageItem.getW();
        int height = imageItem.getH();


        jsonGenerator.writeStringField("url", fullUrl);
        jsonGenerator.writeNumberField("width", width);
        jsonGenerator.writeNumberField("height", height);
        jsonGenerator.writeEndObject();
    }
}
