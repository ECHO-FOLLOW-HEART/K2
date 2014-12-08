package formatter.travelpi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.Map;

/**
 * 将ImageItem序列化成url字符串
 *
 * @author Zephyre
 */
public class ImageItemPlainSerializer extends JsonSerializer<ImageItem> {
    private ImageSizeDesc sizeDesc;

    public ImageItemPlainSerializer() {
        this(ImageSizeDesc.MEDIUM);
    }

    public ImageItemPlainSerializer(ImageSizeDesc sz) {
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

        jsonGenerator.writeString(imgUrl);
    }
}
