package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Locality;
import models.misc.ImageItem;

import java.io.IOException;
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
        DETAILED
    }

    @Override
    public void serialize(Locality locality, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        writeObjectId(locality, jsonGenerator, serializerProvider);
        jsonGenerator.writeStringField("zhName", getString(locality.getZhName()));
        jsonGenerator.writeStringField("enName", getString(locality.getEnName()));

        jsonGenerator.writeEndObject();
    }
}
