package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Locality;

import java.io.IOException;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleLocalitySerializer extends AizouSerializer<Locality> {
    @Override
    public void serialize(Locality locality, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartObject();

        writeObjectId(locality, jgen, serializerProvider);

        jgen.writeStringField("zhName", getString(locality.getZhName()));
        jgen.writeStringField("enName", getString(locality.getEnName()));

        jgen.writeEndObject();
    }
}
