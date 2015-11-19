package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class TerseLocalityFormatter extends AizouFormatter<Locality> {

    public TerseLocalityFormatter() {
        registerSerializer(Locality.class, new TerseLocalitySerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer());
        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList(
                AizouBaseEntity.FD_ID,
                AizouBaseEntity.FD_IS_FAVORITE,
                Locality.FD_EN_NAME,
                Locality.FD_ZH_NAME,
                Locality.fnDesc,
                Locality.fnLocation,
                Locality.fnImages,
                Locality.fnTimeCostDesc,
                Locality.fnTravelMonth,
                Locality.fnImageCnt
        ));
    }

    class TerseLocalitySerializer extends AizouSerializer<Locality> {

        @Override
        public void serialize(Locality locality, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();
            writeObjectId(locality, jsonGenerator, serializerProvider);
            jsonGenerator.writeStringField("zhName", getString(locality.getZhName()));
            jsonGenerator.writeStringField("enName", getString(locality.getEnName()));

            // jsonGenerator.writeStringField(Locality.fnTimeCostDesc, getString(locality.getTimeCostDesc()));
            jsonGenerator.writeStringField(Locality.fnTravelMonth, getString(locality.getTravelMonth()));
            jsonGenerator.writeNumberField(Locality.fnImageCnt, getValue(locality.getImages() == null ? 0 : locality.getImages().size()));

            // images
            jsonGenerator.writeFieldName("images");
            List<ImageItem> images = TaoziDataFilter.getOneImage(locality.getImages());
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


