package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.DetailsEntry
import models.misc.ImageItem
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/28.
 */
class DetailsEntrySerializerScala extends JsonSerializer[DetailsEntry] {
  override def serialize(detailsEntry: DetailsEntry, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("title", detailsEntry.getTitle)
    gen.writeStringField("desc", detailsEntry.getDesc)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(detailsEntry.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeEndObject()
  }
}
