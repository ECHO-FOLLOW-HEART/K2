package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.geo.Locality;
import models.misc.ImageItem;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.List;

/**
 * Created by zephyre on 1/20/15.
 */
public class SearchLocalityFormatter extends AizouFormatter<Locality> {
    public SearchLocalityFormatter() {
        registerSerializer(Locality.class, new SearchLocalitySerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(200));
        initObjectMapper(null);

    }

    class SearchLocalitySerializer extends AizouSerializer<Locality> {
        @Override
        public void serialize(Locality locality, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();
            writeObjectId(locality, jgen, serializerProvider);
            jgen.writeStringField("zhName", getString(locality.getZhName()));
            jgen.writeStringField("enName", getString(locality.getEnName()));
            jgen.writeStringField("desc", getString(locality.getDesc()));
            // images
            jgen.writeFieldName("images");
            List<ImageItem> images = TaoziDataFilter.getOneImage(locality.getImages()) ;
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
    }
}
