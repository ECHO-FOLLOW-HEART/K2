package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.Continent

/**
 * Created by pengyt on 2015/8/27.
 */
class ContinentSerializerScala extends JsonSerializer[Continent] {

  override def serialize(continent: Continent, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", continent.getId.toString)
    gen.writeStringField("zhName", continent.getZhName)
    gen.writeStringField("enName", continent.getEnName)
    gen.writeStringField("code", continent.getCode)

    gen.writeEndObject()
  }
}
