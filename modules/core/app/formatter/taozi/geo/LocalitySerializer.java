package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import formatter.taozi.ObjectIdSerializer;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zephyre on 12/6/14.
 */
public class LocalitySerializer extends AizouSerializer<Locality> {

    private Level level;

    public LocalitySerializer() {
        this(Level.SIMPLE);
    }

    public LocalitySerializer(Level level) {
        this.level = level;
    }

    public enum Level {
        SIMPLE,
        DETAILED
    }

    @Override
    public void serialize(Locality locality, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeFieldName("id");
        new ObjectIdSerializer().serialize(locality.getId(),jsonGenerator,serializerProvider);

        getOidWriter().write(locality.getId(), jsonGenerator);

        jsonGenerator.writeStringField("zhName", getString(locality.getZhName()));
        jsonGenerator.writeStringField("enName", getString(locality.getEnName()));
        if (level.equals(Level.DETAILED)) {
            jsonGenerator.writeBooleanField(Locality.FD_IS_FAVORITE, locality.getIsFavorite());
            jsonGenerator.writeStringField(Locality.fnDesc, getString(locality.getDesc()));
            jsonGenerator.writeStringField(Locality.fnTimeCostDesc, getString(locality.getTimeCostDesc()));
            jsonGenerator.writeStringField(Locality.fnTravelMonth, getString(locality.getTravelMonth()));

            // images
            List<ImageItem> images = locality.getImages();
            if (images==null)
                images=new ArrayList<>();
            jsonGenerator.writeNumberField(Locality.fnImageCnt, images.size());

            jsonGenerator.writeFieldName("images");
            jsonGenerator.writeStartArray();
            if (!images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jsonGenerator, serializerProvider);
            }
            jsonGenerator.writeEndArray();

            // Location
            jsonGenerator.writeFieldName(Locality.fnLocation);
            GeoJsonPoint geoJsonPoint = locality.getLocation();
            JsonSerializer<Object> retLocalition;
            if (geoJsonPoint != null) {
                retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
                retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
            } else {
                retLocalition = serializerProvider.findNullValueSerializer(null);
                retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
            }
        }

        jsonGenerator.writeEndObject();
    }
}
