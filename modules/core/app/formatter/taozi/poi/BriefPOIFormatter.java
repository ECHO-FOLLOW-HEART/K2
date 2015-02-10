package formatter.taozi.poi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.poi.AbstractPOI;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by zephyre on 1/20/15.
 */
public class BriefPOIFormatter extends AizouFormatter<AbstractPOI> {

    public BriefPOIFormatter() {
        registerSerializer(AbstractPOI.class, new BriefPOISerializer());
        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME));
    }

    class BriefPOISerializer extends AizouSerializer<AbstractPOI> {

        @Override
        public void serialize(AbstractPOI abstractPOI, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();

            writeObjectId(abstractPOI, jsonGenerator, serializerProvider);
            jsonGenerator.writeStringField(AbstractPOI.FD_ZH_NAME, getString(abstractPOI.zhName));
            jsonGenerator.writeStringField(AbstractPOI.FD_EN_NAME, getString(abstractPOI.enName));
            jsonGenerator.writeEndObject();
        }
    }
}
