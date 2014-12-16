package formatter.taozi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zephyre on 12/6/14.
 */
public class ImageItemSerializer extends JsonSerializer<ImageItem> {
    private ImageSizeDesc sizeDesc;

    public ImageItemSerializer() {
        this(ImageSizeDesc.MEDIUM);
    }

    public ImageItemSerializer(ImageSizeDesc sz) {
        sizeDesc = sz;
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
        Map<String, Integer> cropHint = imageItem.getCropHint();

        String imgUrl;
        int width = imageItem.getW();
        int height = imageItem.getH();

        if (sizeDesc == ImageSizeDesc.FULL)
            imgUrl = fullUrl;
        else {
            int maxWidth;
            switch (sizeDesc) {
                case SMALL:
                    maxWidth = 400;
                    break;
                case MEDIUM:
                    maxWidth = 640;
                    break;
                case LARGE:
                default:
                    maxWidth = 960;
                    break;
            }

            if (cropHint == null) {
                imgUrl = String.format("%s?imageView2/2/w/%d", fullUrl, maxWidth);
                double r = (double) height / width;
                width = maxWidth;
                height = (int) (width * r);
            } else {
                int top = cropHint.get("top");
                int right = cropHint.get("right");
                int bottom = cropHint.get("bottom");
                int left = cropHint.get("left");

                width = right - left;
                height = bottom - top;

                imgUrl = String.format("%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!%dx%da%da%d/thumbnail/%dx",
                        fullUrl, width, height, left, top, maxWidth);
            }
        }

        jsonGenerator.writeStringField("url", imgUrl);
        jsonGenerator.writeNumberField("width", width);
        jsonGenerator.writeNumberField("height", height);
        jsonGenerator.writeEndObject();
    }
}
