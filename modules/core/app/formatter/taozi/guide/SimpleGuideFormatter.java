package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.GeoJsonPointSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import formatter.taozi.geo.LocalitySerializer;
import formatter.taozi.poi.POISerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Restaurant;
import models.poi.Shopping;

import java.io.IOException;
import java.util.*;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class SimpleGuideFormatter extends AizouFormatter<Guide> {

    public SimpleGuideFormatter(int imgWidth) {
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

    class SimpleGuideSerializer extends AizouSerializer<Guide> {
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
            jgen.writeNumberField(Guide.fnUpdateTime, getValue(guide.updateTime));
            jgen.writeNumberField(Guide.fnDayCnt, getValue(guide.dayCnt));
            jgen.writeStringField(Guide.fnSummary, getString(guide.summary));

            jgen.writeEndObject();
        }
    }
}