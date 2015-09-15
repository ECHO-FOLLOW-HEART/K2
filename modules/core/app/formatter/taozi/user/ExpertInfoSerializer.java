package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.user.ExpertInfo;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;

public class ExpertInfoSerializer extends AizouSerializer<ExpertInfo> {

    @Override
    public void serialize(ExpertInfo expertInfo, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartObject();

        jgen.writeNumberField(ExpertInfo.fnUserId, getValue(expertInfo.getUserId()));
        jgen.writeStringField(ExpertInfo.fnProfile, getString(expertInfo.getProfile()));

        jgen.writeFieldName(ExpertInfo.fnTags);
        jgen.writeStartArray();
        List<String> tags = expertInfo.getTags();
        if (tags != null && !tags.isEmpty()) {
            JsonSerializer<Object> retTag = serializerProvider.findValueSerializer(String.class, null);
            for (String role : tags) {
                retTag.serialize(role, jgen, serializerProvider);
            }
        }
        jgen.writeEndArray();

        jgen.writeFieldName(ExpertInfo.fnZone);
        jgen.writeStartArray();
        List<ObjectId> zone = expertInfo.getZone();
        if (tags != null && !tags.isEmpty()) {
            JsonSerializer<Object> retTag = serializerProvider.findValueSerializer(String.class, null);
            for (ObjectId role : zone) {
                retTag.serialize(role.toString(), jgen, serializerProvider);
            }
        }
        jgen.writeEndArray();

        jgen.writeEndObject();
    }
}
