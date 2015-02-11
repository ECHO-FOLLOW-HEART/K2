package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.geo.LocalitySerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.SimpleRef;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by topy
 */
public class SimpleRefFormatter extends AizouFormatter<SimpleRef> {

    public SimpleRefFormatter() {

        registerSerializer(Locality.class, new LocalitySerializer());
        registerSerializer(SimpleRef.class, new SimpleRefSerializer());
        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                "zhName",
                "enName"
        );
    }

    class SimpleRefSerializer extends AizouSerializer<SimpleRef> {

        @Override
        public void serialize(SimpleRef simpleRef, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();

            writeObjectId(simpleRef, jsonGenerator, serializerProvider);
            jsonGenerator.writeStringField(SimpleRef.simpZhName, getString(simpleRef.getZhName()));
            jsonGenerator.writeStringField(SimpleRef.simpEnName, getString(simpleRef.getEnName()));
            jsonGenerator.writeEndObject();

        }
    }


}
