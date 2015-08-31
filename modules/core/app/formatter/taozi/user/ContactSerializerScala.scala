package formatter.taozi.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.user.Contact

/**
 * Created by pengyt on 2015/8/31.
 */
class ContactSerializerScala extends JsonSerializer[Contact] {

  override def serialize(contact: Contact, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("entryId", contact.getEntryId)
    gen.writeNumberField("sourceId", contact.getSourceId)
    gen.writeBooleanField("isUser", contact.isUser)
    gen.writeBooleanField("isContact", contact.isContact)
    gen.writeNumberField("userId", contact.getUserId)

    gen.writeStringField("name", contact.getName)
    gen.writeStringField("tel", contact.getTel)
    gen.writeStringField("weixin", contact.getWeixin)

    gen.writeEndObject()
  }

}
