package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.geo.DetailsEntry;

import java.io.IOException;
import java.util.Arrays;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class DetailsEntryFormatter extends AizouFormatter<DetailsEntry> {

    public DetailsEntryFormatter() {
        registerSerializer(DetailsEntry.class, new DetailsEntrySerializer());

        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList("title", "desc"));
    }

    class DetailsEntrySerializer extends AizouSerializer<DetailsEntry> {
        @Override
        public void serialize(DetailsEntry d, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();
            jgen.writeStringField("title", getString(d.getTitle()));
            jgen.writeStringField("desc", getString(d.getDesc()));
            jgen.writeEndObject();
        }
    }
}
