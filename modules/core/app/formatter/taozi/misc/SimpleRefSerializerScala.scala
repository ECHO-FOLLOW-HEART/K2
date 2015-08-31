package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.SimpleRef

/**
 * Created by pengyt on 2015/8/31.
 */
class SimpleRefSerializerScala extends JsonSerializer[SimpleRef] {

  override def serialize(simpleRef: SimpleRef, gen: JsonGenerator, serializers: SerializerProvider): Unit = {

    gen.writeStartObject()

    gen.writeStringField("id", simpleRef.getId.toString)
    gen.writeStringField(SimpleRef.simpZhName, simpleRef.getZhName)
    gen.writeStringField(SimpleRef.simpEnName, simpleRef.getEnName)

    gen.writeEndObject()
  }

}
