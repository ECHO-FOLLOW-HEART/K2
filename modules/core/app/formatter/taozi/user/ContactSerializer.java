package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.user.Contact;

import java.io.IOException;

/**
 * Created by zephyre on 1/20/15.
 */
public class ContactSerializer extends AizouSerializer<Contact> {
    @Override
    public void serialize(Contact contact, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("entryId", contact.getEntryId());
        jgen.writeObjectField("sourceId", contact.getSourceId());
        jgen.writeObjectField("isUser", contact.isUser());
        jgen.writeObjectField("isContact", contact.isContact());
        jgen.writeObjectField("userId", contact.getUserId());

        jgen.writeStringField("name", getString(contact.getName()));
        jgen.writeStringField("tel", getString(contact.getTel()));
        jgen.writeStringField("weixin", getString(contact.getWeixin()));

        jgen.writeEndObject();

    }
}
