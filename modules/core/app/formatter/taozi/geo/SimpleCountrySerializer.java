package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Country;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.List;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleCountrySerializer extends AizouSerializer<Country> {
    @Override
    public void serialize(Country country, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartObject();

        writeObjectId(country, jgen, serializerProvider);

        jgen.writeFieldName("images");
        List<ImageItem> images = country.getImages();
        jgen.writeStartArray();
        if (images != null && !images.isEmpty()) {
            JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
            for (ImageItem image : images)
                ret.serialize(image, jgen, serializerProvider);
        }
        jgen.writeEndArray();

        jgen.writeStringField("zhName", getString(country.getZhName()));
        jgen.writeStringField("enName", getString(country.getEnName()));
//        jgen.writeStringField("code", getString(country.getCode()).toUpperCase());
//        jgen.writeStringField("desc", getString(country.getDesc()));

        jgen.writeEndObject();
    }
}
