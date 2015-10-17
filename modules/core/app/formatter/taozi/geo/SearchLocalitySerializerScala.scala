package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.Locality
import models.misc.ImageItem
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/10/15.
 */
class SearchLocalitySerializerScala extends JsonSerializer[Locality] {

  override def serialize(locality: Locality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", locality.getId.toString)
    gen.writeStringField("zhName", locality.getZhName)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val imagesOpt = Option(locality.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
