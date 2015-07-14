package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.geo.RmdLocality;
import models.geo.RmdProvince;
import models.guide.ItinerItem;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 推荐目的地的省份Formatter
 * <p>
 * Created by topy on 3/24/15.
 */
public class RmdProvinceFormatter extends AizouFormatter<RmdProvince> {
    public RmdProvinceFormatter() {
        registerSerializer(RmdProvince.class, new RmdProvinceSerializer());
        registerSerializer(RmdLocality.class, new RmdLocalitySerializer());
        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList(RmdProvince.FD_ID, RmdProvince.FD_ZH_NAME, RmdProvince.FD_EN_NAME));
    }

    class RmdProvinceSerializer extends AizouSerializer<RmdProvince> {
        @Override
        public void serialize(RmdProvince rmdProvince, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(rmdProvince, jgen, serializerProvider);

            jgen.writeStringField(RmdProvince.FD_ZH_NAME, getString(rmdProvince.getZhName()));
            jgen.writeStringField(RmdProvince.FD_EN_NAME, getString(rmdProvince.getEnName()));
            jgen.writeStringField(RmdProvince.FD_EN_PINYIN, getString(rmdProvince.getPinyin()));

            jgen.writeFieldName(RmdProvince.FD_EN_DESTINATION);
            List<RmdLocality> localityList = rmdProvince.getDestinations();
            jgen.writeStartArray();
            if (localityList != null && !localityList.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(RmdLocality.class, null);
                for (RmdLocality rmdLocality : localityList)
                    ret.serialize(rmdLocality, jgen, serializerProvider);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }

    class RmdLocalitySerializer extends AizouSerializer<RmdLocality> {

        @Override
        public void serialize(RmdLocality rmdLocality, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();
            writeObjectId(rmdLocality, jsonGenerator, serializerProvider);
            jsonGenerator.writeStringField(RmdLocality.FD_ZH_NAME, getString(rmdLocality.getZhName()));

            // Location
            jsonGenerator.writeFieldName(Locality.fnLocation);
            GeoJsonPoint geoJsonPoint = rmdLocality.getLocation();
            JsonSerializer<Object> retLocalition;
            if (geoJsonPoint != null) {
                retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
                retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
            } else {
                retLocalition = serializerProvider.findNullValueSerializer(null);
                retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
            }

            // images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = TaoziDataFilter.getOneImage(rmdLocality.getImages());
            jsonGenerator.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jsonGenerator, serializerProvider);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();

        }
    }

}
