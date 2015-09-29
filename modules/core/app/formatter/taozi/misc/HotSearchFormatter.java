package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.misc.HotSearch;

import java.io.IOException;

/**
 * Created by topy
 */
public class HotSearchFormatter extends AizouFormatter<HotSearch> {

    public HotSearchFormatter() {

        registerSerializer(HotSearch.class, new HotSearchSerializer());
        initObjectMapper(null);

//        filteredFields = new HashSet<>();
//        Collections.addAll(filteredFields,
//                AizouBaseEntity.FD_ID,
//                HotSearch.fnItemId,
//                "zhName",
//                "enName"
//        );
    }

    class HotSearchSerializer extends AizouSerializer<HotSearch> {

        @Override
        public void serialize(HotSearch simpleRef, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartObject();

            //writeObjectId(simpleRef, jsonGenerator, serializerProvider);
            //ObjectId itemId = simpleRef.getItemId();
            //jsonGenerator.writeStringField(HotSearch.FD_ITEMID, getString(itemId == null ? "" : itemId.toString()));
            //jsonGenerator.writeStringField(HotSearch.FD_SEARCHTYPE, getString(simpleRef.getSearchType()));
            //jsonGenerator.writeStringField(HotSearch.FD_SEARCHFIELD, getString(simpleRef.getSearchField()));
            jsonGenerator.writeStringField(HotSearch.FD_SEARCHCONTENT, getString(simpleRef.getItemName()));
            jsonGenerator.writeStringField("zhName", getString(simpleRef.getItemName()));
            jsonGenerator.writeEndObject();

        }
    }


}
