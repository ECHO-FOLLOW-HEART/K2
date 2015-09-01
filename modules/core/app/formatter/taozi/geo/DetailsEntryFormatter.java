package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.geo.DetailsEntry;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lxf on 14-11-1.
 */
public class DetailsEntryFormatter extends AizouFormatter<DetailsEntry> {

    public DetailsEntryFormatter(Integer imgWidth) {
        registerSerializer(DetailsEntry.class, new DetailsEntrySerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(
                "images",
                "title",
                "desc"));
    }

    class DetailsEntrySerializer extends AizouSerializer<DetailsEntry> {
        @Override
        public void serialize(DetailsEntry detailsEntry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("title", getString(detailsEntry.getTitle()));
            jsonGenerator.writeStringField("desc", getString(detailsEntry.getDesc()));
            // images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = detailsEntry.getImages();
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
