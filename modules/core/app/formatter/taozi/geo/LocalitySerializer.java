package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        DETAILED,
        FORTRACKS
    }

    @Override
    public void serialize(Locality locality, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        writeObjectId(locality, jsonGenerator, serializerProvider);
        jsonGenerator.writeStringField("zhName", getString(locality.getZhName()));
        jsonGenerator.writeStringField("enName", getString(locality.getEnName()));

        if(level.equals(Level.FORTRACKS)){
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
        if (level.equals(Level.DETAILED)) {
            jsonGenerator.writeBooleanField(Locality.FD_IS_FAVORITE, getValue(locality.getIsFavorite()));
            jsonGenerator.writeStringField(Locality.fnDesc, getString(locality.getDesc()));
            jsonGenerator.writeStringField(Locality.fnTimeCostDesc, getString(locality.getTimeCostDesc()));
            jsonGenerator.writeStringField(Locality.fnTravelMonth, getString(locality.getTravelMonth()));
            jsonGenerator.writeNumberField(Locality.fnImageCnt, getValue(locality.getImages() == null ? 0 : locality.getImages().size()));

            // images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = locality.getImages();
            jsonGenerator.writeStartArray();
            if (images != null && !images.isEmpty()) {
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
