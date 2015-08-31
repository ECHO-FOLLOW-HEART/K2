package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.AizouBaseEntity
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import models.poi.{ Restaurant, AbstractPOI }
import org.apache.commons.lang3.StringUtils
import utils.{ Constants, TaoziDataFilter }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class RestaurantPOISerializerScala extends JsonSerializer[Restaurant] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(restaurant: Restaurant, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", restaurant.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, restaurant.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, restaurant.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, restaurant.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, restaurant.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = restaurant.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")

    val imagesOpt = Option(if (level.equals(Level.SIMPLE)) TaoziDataFilter.getOneImage(restaurant.getImages)
    else restaurant.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeStringField("type", "restaurant") //这个字段还有必要么？
    gen.writeStringField(AbstractPOI.FD_PRICE_DESC, TaoziDataFilter.getPriceDesc(restaurant))
    gen.writeNumberField(AbstractPOI.FD_PRICE, restaurant.price)

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(restaurant.getRank))

    // Locality
    gen.writeFieldName(AbstractPOI.FD_LOCALITY)
    val localities = restaurant.getLocality
    val retLocality = if (localities != null) serializers.findValueSerializer(classOf[Locality], null)
    else serializers.findNullValueSerializer(null)
    retLocality.serialize(localities, gen, serializers)

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = restaurant.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = restaurant.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, restaurant.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, StringUtils.abbreviate(restaurant.desc, Constants.ABBREVIATE_LEN))

      // Tel
      val tels = restaurant.tel
      gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
      gen.writeStartArray()
      if (tels != null && (!tels.isEmpty)) {
        for (tel <- tels)
          gen.writeString(tel)
      }
      gen.writeEndArray()
    }

    gen.writeEndObject()
  }

  // Polymorphic Single Bean Use
  override def serializeWithType(restaurant: Restaurant, gen: JsonGenerator, serializers: SerializerProvider, typeSer: TypeSerializer): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", restaurant.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, restaurant.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, restaurant.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, restaurant.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, restaurant.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = restaurant.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")
    val images = restaurant.getImages
    gen.writeStartArray()
    if (images != null && !images.isEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeStringField("type", "restaurant") // 可以不需要这个字段了？
    gen.writeStringField(AbstractPOI.FD_PRICE_DESC, TaoziDataFilter.getPriceDesc(restaurant))
    gen.writeNumberField(AbstractPOI.FD_PRICE, restaurant.price)

    // Tel
    val tels = restaurant.tel
    gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
    gen.writeStartArray()
    if (tels != null && (!tels.isEmpty)) {
      for (tel <- tels)
        gen.writeString(tel)
    }
    gen.writeEndArray()

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(restaurant.getRank.toInt))

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = restaurant.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = restaurant.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, restaurant.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, restaurant.desc)
    }
    gen.writeEndObject()
  }

  def checkRank(rank: Int): Int = {
    if (rank >= 999) 0 else rank
  }
}

object RestaurantPOISerializerScala {

  def apply(): RestaurantPOISerializerScala = {
    apply(Level.SIMPLE)
  }

  def apply(level: Level.Value): RestaurantPOISerializerScala = {
    val result = new RestaurantPOISerializerScala
    result.level = level
    result
  }
}