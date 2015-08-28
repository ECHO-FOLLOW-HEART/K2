package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.Continent

/**
 * Created by pengyt on 2015/8/27.
 */
class ContinentSerializerScala extends JsonSerializer[Continent] {

  override def serialize(value: Continent, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", value.getId.toString)
    gen.writeStringField("zhName", value.getZhName)
    gen.writeStringField("enName", value.getEnName)
    gen.writeStringField("code", value.getCode)

    gen.writeEndObject()
  }
}
