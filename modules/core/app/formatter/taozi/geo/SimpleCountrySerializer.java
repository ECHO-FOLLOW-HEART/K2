package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Country;
import models.misc.ImageItem;
import utils.TaoziDataFilter;

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
        List<ImageItem> images = TaoziDataFilter.getOneImage(country.getImages());
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
        jgen.writeNumberField("rank", country.getRank() == null ? 100 : country.getRank());

//        jgen.writeFieldName("continents");
//        String code = country.getContCode();
//        String zhCont = country.getZhCont();
//        String enCont = country.getEnCont();
//        Continent continent = new Continent();
//        continent.setId(new ObjectId());
//        continent.setCode(code);
//        continent.setZhName(zhCont);
//        continent.setEnName(enCont);
//        JsonSerializer<Object> retContinent;
//        if (code != null && zhCont != null && enCont != null) {
//            retContinent = serializerProvider.findValueSerializer(Continent.class, null);
//            retContinent.serialize(continent, jgen, serializerProvider);
//        } else {
//            retContinent = serializerProvider.findNullValueSerializer(null);
//            retContinent.serialize(continent, jgen, serializerProvider);
//        }

        // TODO 商品数量
        jgen.writeNumberField("commoditiesCnt", 0);
//        jgen.writeStringField("code", getString(country.getCode()).toUpperCase());
//        jgen.writeStringField("desc", getString(country.getDesc()));

        jgen.writeEndObject();
    }
}
