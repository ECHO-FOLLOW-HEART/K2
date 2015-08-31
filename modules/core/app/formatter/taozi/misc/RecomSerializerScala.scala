package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.Recom

/**
 * Created by pengyt on 2015/8/31.
 */
class RecomSerializerScala extends JsonSerializer[Recom] {

  override def serialize(recom: Recom, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField(Recom.fnItemId, recom.itemId)
    gen.writeStringField(Recom.fnTitle, recom.title)
    gen.writeStringField(Recom.fnItemType, recom.itemType)
    gen.writeStringField(Recom.fnLinkType, recom.linkType)
    gen.writeStringField(Recom.fnLinkUrl, recom.linkUrl)
    gen.writeStringField(Recom.fnDesc, recom.desc)
    gen.writeStringField(Recom.fnCover, recom.cover)
    gen.writeEndObject()
  }
}
