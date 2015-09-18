package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.misc.HotSearch;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by topy
 */
public class HotSearchFormatter extends AizouFormatter<HotSearch> {

    public HotSearchFormatter() {

        registerSerializer(HotSearch.class, new HotSearchSerializer());
        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                HotSearch.fnItemId,
                "zhName",
                "enName"
        );
    }

    class HotSearchSerializer extends AizouSerializer<HotSearch> {

        @Override
        public void serialize(HotSearch simpleRef, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();

            writeObjectId(simpleRef, jsonGenerator, serializerProvider);
            jsonGenerator.writeStringField(HotSearch.fnItemId, getString(simpleRef.getItemId().toString()));
            jsonGenerator.writeStringField(HotSearch.fnZhName,getString(simpleRef.getZhName()));
            jsonGenerator.writeEndObject();

        }
    }


}
