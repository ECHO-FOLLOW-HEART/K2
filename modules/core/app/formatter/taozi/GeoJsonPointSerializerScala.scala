package formatter.taozi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.GeoJsonPoint

/**
 * Created by pengyt on 2015/8/29.
 */
class GeoJsonPointSerializerScala extends JsonSerializer[GeoJsonPoint] {

  override def serialize(geoJsonPoint: GeoJsonPoint, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    // Location
    gen.writeFieldName(GeoJsonPoint.FD_COORDS)
    gen.writeStartArray()
    if (geoJsonPoint != null && geoJsonPoint.getCoordinates() != null && geoJsonPoint.getCoordinates().length >= 2) {
      gen.writeNumber(geoJsonPoint.getCoordinates.toSeq(0))
      gen.writeNumber(geoJsonPoint.getCoordinates.toSeq(1))
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
