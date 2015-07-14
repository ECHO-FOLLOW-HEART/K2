package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleLocalityWithLocationFormatter extends AizouFormatter<Locality> {

    public SimpleLocalityWithLocationFormatter(Integer imgWidth) {
        registerSerializer(Locality.class, new SimpleLocalityWithLocationSerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnLocation,Locality.fnImages));
    }

    class SimpleLocalityWithLocationSerializer extends AizouSerializer<Locality> {
        @Override
        public void serialize(Locality locality, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(locality, jgen, serializerProvider);

            jgen.writeStringField("zhName", getString(locality.getZhName()));
            jgen.writeStringField("enName", getString(locality.getEnName()));

            // images
            jgen.writeFieldName("images");
            List<ImageItem> images = TaoziDataFilter.getOneImage(locality.getImages());
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            // Location
            jgen.writeFieldName(Locality.fnLocation);
            GeoJsonPoint geoJsonPoint = locality.getLocation();
            JsonSerializer<Object> retLocalition;
            if (geoJsonPoint != null) {
                retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
                retLocalition.serialize(geoJsonPoint, jgen, serializerProvider);
            } else {
                retLocalition = serializerProvider.findNullValueSerializer(null);
                retLocalition.serialize(geoJsonPoint, jgen, serializerProvider);
            }

            jgen.writeEndObject();
        }
    }
}
