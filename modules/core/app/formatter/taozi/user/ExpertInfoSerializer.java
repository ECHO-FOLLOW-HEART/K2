package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.user.ExpertInfo;
import models.user.UserInfo;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.List;

public class ExpertInfoSerializer extends AizouSerializer<ExpertInfo> {

    @Override
    public void serialize(ExpertInfo expertInfo, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartObject();

        jgen.writeNumberField(ExpertInfo.fnUserId, getValue(expertInfo.getUserId()));

        jgen.writeEndArray();

        jgen.writeEndObject();
    }
}
