package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem
import models.poi.{ Shopping, AbstractPOI }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class SearchShoppingSerializerScala extends JsonSerializer[Shopping] {

  override def serialize(shopping: Shopping, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", shopping.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, shopping.getZhName)

    gen.writeFieldName("images")
    gen.writeStartArray()
    val imagesOpt = Option(shopping.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField(AbstractPOI.FD_RATING, shopping.getRating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, shopping.getAddress)
    gen.writeStringField(AbstractPOI.FD_STYLE, shopping.getStyle)

    gen.writeEndObject()
  }
}