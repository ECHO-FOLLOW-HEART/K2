package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.AizouBaseEntity
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import models.poi.{ Hotel, AbstractPOI }
import org.apache.commons.lang3.StringUtils
import utils.{ Constants, TaoziDataFilter }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class HotelPOISerializerScala extends JsonSerializer[Hotel] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(hotel: Hotel, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", hotel.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, hotel.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, hotel.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, hotel.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, hotel.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = hotel.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")

    val imagesOpt = Option(if (level.equals(Level.SIMPLE)) TaoziDataFilter.getOneImage(hotel.getImages)
    else hotel.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeStringField("type", "hotel")

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(hotel.getRank.toInt))

    // Locality
    gen.writeFieldName(AbstractPOI.FD_LOCALITY)
    val localities = hotel.getLocality
    val retLocality = if (localities != null) serializers.findValueSerializer(classOf[Locality], null)
    else serializers.findNullValueSerializer(null)
    retLocality.serialize(localities, gen, serializers)

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = hotel.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = hotel.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, hotel.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, StringUtils.abbreviate(hotel.desc, Constants.ABBREVIATE_LEN))

      // Tel
      val tels = hotel.tel
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
  override def serializeWithType(hotel: Hotel, gen: JsonGenerator, serializers: SerializerProvider, typeSer: TypeSerializer): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", hotel.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, hotel.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, hotel.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, hotel.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, hotel.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = hotel.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")
    val images = hotel.getImages
    gen.writeStartArray()
    if (images != null && !images.isEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeStringField("type", "hotel") // 可以不需要了

    // Tel
    val tels = hotel.tel
    gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
    gen.writeStartArray()
    if (tels != null && (!tels.isEmpty)) {
      for (tel <- tels)
        gen.writeString(tel)
    }
    gen.writeEndArray()

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(hotel.getRank.toInt))

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = hotel.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = hotel.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, hotel.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, hotel.desc)

    }
    gen.writeEndObject()
  }

  def checkRank(rank: Int): Int = {
    if (rank >= 999) 0 else rank
  }
}

object HotelPOISerializerScala {

  def apply(): HotelPOISerializerScala = {
    apply(Level.SIMPLE)
  }

  def apply(level: Level.Value): HotelPOISerializerScala = {
    val result = new HotelPOISerializerScala
    result.level = level
    result
  }
}