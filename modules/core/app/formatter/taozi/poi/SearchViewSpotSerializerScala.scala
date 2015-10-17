package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem
import models.poi.{ AbstractPOI, ViewSpot }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class SearchViewSpotSerializerScala extends JsonSerializer[ViewSpot] {

  override def serialize(viewSpot: ViewSpot, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", viewSpot.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, viewSpot.getZhName)

    gen.writeFieldName("images")
    gen.writeStartArray()
    val imagesOpt = Option(viewSpot.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField(AbstractPOI.FD_RATING, viewSpot.getRating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, viewSpot.getAddress)

    gen.writeEndObject()
  }

}