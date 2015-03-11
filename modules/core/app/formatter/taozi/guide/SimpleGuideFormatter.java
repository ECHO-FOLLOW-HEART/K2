package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class SimpleGuideFormatter extends AizouFormatter<Guide> {

    public SimpleGuideFormatter(Integer imgWidth) {
        registerSerializer(Guide.class, new SimpleGuideSerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AbstractGuide.fdId,
                AbstractGuide.fnTitle,
                Guide.fnUpdateTime,
                AbstractGuide.fnImages
        );
    }

    private class SimpleGuideSerializer extends AizouSerializer<Guide> {
        @Override
        public void serialize(Guide guide, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(guide, jgen, serializerProvider);

            // Images
            jgen.writeFieldName("images");
            List<ImageItem> images = guide.getImages();
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            jgen.writeStringField(Guide.fnTitle, getString(guide.title));
            jgen.writeObjectField(Guide.fnUpdateTime, getValue(guide.getUpdateTime()));
            jgen.writeObjectField(Guide.fnDayCnt, getValue(guide.getDayCnt()));
            jgen.writeStringField(Guide.fnSummary, getString(guide.getSummary()));

            jgen.writeEndObject();
        }
    }
}