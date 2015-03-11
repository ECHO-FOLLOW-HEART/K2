package formatter.taozi.poi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import formatter.taozi.geo.SimpleCountrySerializer;
import models.AizouBaseEntity;
import models.geo.Country;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.POIRmd;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 返回POI的推荐
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class POIRmdFormatter extends AizouFormatter<POIRmd> {


    public void setImageWidth(int maxWidth) {
        imageItemSerializer.setWidth(maxWidth);
    }

    private ImageItemSerializer imageItemSerializer;

    public POIRmdFormatter() {
        registerSerializer(POIRmd.class, new POIRmdSerializer());

        imageItemSerializer = new ImageItemSerializer();
        registerSerializer(ImageItem.class, imageItemSerializer);

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, POIRmd.fnTitle,POIRmd.fnImages,POIRmd.fnRating));
    }

    class POIRmdSerializer extends AizouSerializer<POIRmd> {


        @Override
        public void serialize(POIRmd poiRmd, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {
            jgen.writeStartObject();

            writeObjectId(poiRmd, jgen, serializerProvider);

            jgen.writeFieldName("images");
            List<ImageItem> images = poiRmd.getImages();
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            jgen.writeStringField("title", getString(poiRmd.getTitle()));
            jgen.writeNumberField("rating", getValue(poiRmd.getRating()));

            jgen.writeEndObject();
        }
    }

}
