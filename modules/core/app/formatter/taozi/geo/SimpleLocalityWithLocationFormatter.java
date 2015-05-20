package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleLocalityWithLocationFormatter extends AizouFormatter<Locality> {

    public SimpleLocalityWithLocationFormatter() {
        registerSerializer(Locality.class, new SimpleLocalityWithLocationSerializer());
        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME,Locality.FD_EN_NAME,Locality.fnLocation));
    }

    class SimpleLocalityWithLocationSerializer extends AizouSerializer<Locality> {
        @Override
        public void serialize(Locality locality, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(locality, jgen, serializerProvider);

            jgen.writeStringField("zhName", getString(locality.getZhName()));
            jgen.writeStringField("enName", getString(locality.getEnName()));
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
