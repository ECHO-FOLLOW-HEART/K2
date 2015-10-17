package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem
import models.poi.{ Restaurant, AbstractPOI }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class SearchRestaurantSerializerScala extends JsonSerializer[Restaurant] {

  override def serialize(restaurant: Restaurant, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", restaurant.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, restaurant.getZhName)

    gen.writeFieldName("images")
    gen.writeStartArray()
    val imagesOpt = Option(restaurant.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField(AbstractPOI.FD_RATING, restaurant.getRating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, restaurant.getAddress)
    gen.writeStringField(AbstractPOI.FD_STYLE, restaurant.getStyle)

    gen.writeEndObject()
  }
}