package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}
import models.geo.Locality

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleLocalitySerializerScala extends JsonSerializer[Locality]{

  override def serialize(locality: Locality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", locality.getId.toString)
    gen.writeStringField("zhName", locality.getZhName)
    gen.writeStringField("enName", locality.getEnName)

    gen.writeEndObject()
  }
}
