package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Continent;
import models.geo.Country;
import models.geo.Locality;
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

        jgen.writeFieldName("continents");
        String code = country.getContCode();
        String zhCont = country.getZhCont();
        String enCont = country.getEnCont();
        if (code != null && zhCont != null && enCont != null) {
            Continent continent = new Continent();
            continent.setCode(code);
            continent.setZhName(zhCont);
            continent.setEnName(enCont);
            JsonSerializer<Object> retCountry;
            if (country != null) {
                retCountry = serializerProvider.findValueSerializer(Country.class, null);
                retCountry.serialize(country, jgen, serializerProvider);
            } else {
                retCountry = serializerProvider.findNullValueSerializer(null);
                retCountry.serialize(country, jgen, serializerProvider);
            }
        }

//        jgen.writeStringField("code", getString(country.getCode()).toUpperCase());
//        jgen.writeStringField("desc", getString(country.getDesc()));

        jgen.writeEndObject();
    }
}
