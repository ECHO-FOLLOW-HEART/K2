package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.AizouBaseEntity;
import models.geo.Continent;
import models.geo.Country;
import models.geo.CountryExpert;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zephyre on 1/20/15.
 */
public class CountryExpertFormatter extends AizouFormatter<CountryExpert> {

    public void setImageWidth(int maxWidth) {
        imageItemSerializer.setWidth(maxWidth);
    }

    private ImageItemSerializer imageItemSerializer;

    public CountryExpertFormatter() {
        registerSerializer(CountryExpert.class, new CountryExpertSerializer());
        registerSerializer(Continent.class, new SimpleContinentSerializer());

        imageItemSerializer = new ImageItemSerializer();
        registerSerializer(ImageItem.class, imageItemSerializer);

        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME,
                Country.fnDesc, Country.fnCode, Country.fnImages));
    }

    class CountryExpertSerializer extends AizouSerializer<CountryExpert> {
        @Override
        public void serialize(CountryExpert country, JsonGenerator jgen, SerializerProvider serializerProvider)
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
            jgen.writeStringField("code", getString(country.getCode()));
            jgen.writeNumberField("rank", getValue(country.getRank()));
            jgen.writeNumberField("expertCnt", getValue(country.getExpertCnt()));

            jgen.writeFieldName("continents");
            Continent continent = country.getContinent();
            JsonSerializer<Object> retContinent;
            if (continent != null) {
                retContinent = serializerProvider.findValueSerializer(Continent.class, null);
                retContinent.serialize(continent, jgen, serializerProvider);
            } else {
                retContinent = serializerProvider.findNullValueSerializer(null);
                retContinent.serialize(continent, jgen, serializerProvider);
            }
            jgen.writeEndObject();
        }
    }
}
