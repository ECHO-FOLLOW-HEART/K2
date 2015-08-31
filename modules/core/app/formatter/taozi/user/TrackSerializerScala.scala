package formatter.taozi.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.{ ImageItem, Track }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class TrackSerializerScala extends JsonSerializer[Track] {

  override def serialize(track: Track, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", track.getId.toString)
    val locality = track.getLocality
    gen.writeStringField("zhName", locality.getZhName)
    gen.writeStringField("enName", locality.getEnName)

    // images
    gen.writeFieldName("images")
    val images = Option(locality.getImages) map (_.toSeq)
    gen.writeStartArray()
    if (images nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    // Location
    gen.writeFieldName(Locality.fnLocation)
    val geoJsonPoint = locality.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    gen.writeEndObject()
  }

}
