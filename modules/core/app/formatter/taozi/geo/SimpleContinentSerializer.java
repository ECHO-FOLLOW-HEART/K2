package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Continent;

import java.io.IOException;

/**
 * Created by topy on 2015/7/20.
 */
public class SimpleContinentSerializer extends AizouSerializer<Continent> {
    @Override
    public void serialize(Continent continent, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartObject();
        writeObjectId(continent, jgen, serializerProvider);
        jgen.writeStringField("zhName", getString(continent.getZhName().equals("南美洲") ? "美洲" : continent.getZhName()));
        jgen.writeStringField("enName", getString(continent.getEnName()));
        jgen.writeStringField("code", getString(continent.getCode()));
        jgen.writeEndObject();
    }
}