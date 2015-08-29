package formatter.taozi.guide

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.Locality
import models.guide.{ ItinerItem, AbstractGuide, Guide }
import models.misc.ImageItem
import models.poi.{ Restaurant, Shopping }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class GuideSerializerScala extends JsonSerializer[Guide] {

  override def serialize(guide: Guide, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", guide.getId.toString)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(guide.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeStringField(AbstractGuide.fnTitle, guide.title)
    gen.writeStringField(Guide.fnStatus, if (guide.getStatus == null) Guide.fnStatusPlanned else guide.getStatus)
    gen.writeNumberField(Guide.fnUserId, guide.getUserId.toInt)
    gen.writeNumberField(Guide.fnItineraryDays, guide.getItineraryDays.toInt)
    gen.writeNumberField(Guide.fnUpdateTime, guide.getUpdateTime)

    // Locality
    gen.writeFieldName(AbstractGuide.fnLocalities)
    gen.writeStartArray()
    val localities = guide.localities
    if (localities != null && !localities.isEmpty) {
      val retLocality = serializers.findValueSerializer(classOf[Locality], null)
      for (locality <- localities) {
        retLocality.serialize(locality, gen, serializers)
      }
    }
    gen.writeEndArray()

    // Itinerary
    gen.writeFieldName(AbstractGuide.fnItinerary)
    gen.writeStartArray()
    val itinerItems = guide.itinerary
    if (itinerItems != null && !itinerItems.isEmpty) {
      val retItinerItems = serializers.findValueSerializer(classOf[ItinerItem], null)
      for (itinerItem <- itinerItems) {
        if (itinerItem != null && itinerItem.poi != null) {
          retItinerItems.serialize(itinerItem, gen, serializers)
        }
      }
    }
    gen.writeEndArray()

    //Shopping
    gen.writeFieldName(AbstractGuide.fnShopping)
    gen.writeStartArray()
    val shoppingList = guide.shopping
    if (shoppingList != null && !shoppingList.isEmpty) {
      val retShopping = serializers.findValueSerializer(classOf[Shopping], null)
      for (shopping <- shoppingList) {
        retShopping.serialize(shopping, gen, serializers)
      }
    }
    gen.writeEndArray()

    // Restaurant
    gen.writeFieldName(AbstractGuide.fnRestaurant)
    gen.writeStartArray();
    val restaurants = guide.restaurant
    if (restaurants != null && !restaurants.isEmpty) {
      val retRestaurants = serializers.findValueSerializer(classOf[Restaurant], null)
      for (restaurant <- restaurants) {
        retRestaurants.serialize(restaurant, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
