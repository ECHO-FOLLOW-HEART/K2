package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.poi.AbstractPOI

/**
 * Created by pengyt on 2015/8/31.
 */
class BriefPOISerializerScala extends JsonSerializer[AbstractPOI] {

  override def serialize(abstractPOI: AbstractPOI, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", abstractPOI.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, abstractPOI.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, abstractPOI.enName)

    gen.writeEndObject()
  }

}
