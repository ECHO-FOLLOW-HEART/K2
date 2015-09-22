package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.AizouBaseEntity
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import models.poi.{ AbstractPOI, ViewSpot }
import org.apache.commons.lang3.StringUtils
import utils.{ Constants, TaoziDataFilter }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class ViewSpotPOISerializerScala extends JsonSerializer[ViewSpot] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(viewSpot: ViewSpot, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", viewSpot.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, viewSpot.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, viewSpot.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, viewSpot.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, viewSpot.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = viewSpot.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")

    val imagesOpt = Option(if (level.equals(Level.SIMPLE)) TaoziDataFilter.getOneImage(viewSpot.getImages)
    else viewSpot.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeStringField("type", "vs")
    gen.writeStringField(AbstractPOI.FD_TIMECOSTDESC, viewSpot.timeCostDesc)
    gen.writeStringField(AbstractPOI.FD_PRICE_DESC, viewSpot.priceDesc)

    val tels = viewSpot.tel
    gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
    gen.writeStartArray()
    if (tels != null && (!tels.isEmpty)) {
      for (tel <- tels)
        gen.writeString(tel)
    }
    gen.writeEndArray()

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(viewSpot.getRank.toInt))

    // Locality
    gen.writeFieldName(AbstractPOI.FD_LOCALITY)
    val localities = viewSpot.getLocality
    val retLocality = if (localities != null) serializers.findValueSerializer(classOf[Locality], null)
    else serializers.findNullValueSerializer(null)
    retLocality.serialize(localities, gen, serializers)

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = viewSpot.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = viewSpot.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, viewSpot.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, StringUtils.abbreviate(viewSpot.desc, Constants.ABBREVIATE_LEN))

      // Tel
      val tels = viewSpot.tel
      gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
      gen.writeStartArray()
      if (tels != null && (!tels.isEmpty)) {
        for (tel <- tels)
          gen.writeString(tel)
      }
      gen.writeEndArray()

      gen.writeStringField(ViewSpot.FD_OPEN_TIME, viewSpot.openTime)
      gen.writeStringField(ViewSpot.FD_TIME_COST_DESC, viewSpot.getTimeCostDesc)
      gen.writeStringField(ViewSpot.FD_TRAVEL_MONTH, viewSpot.getTravelMonth)
      if (viewSpot.getTrafficInfo == null || viewSpot.getTrafficInfo.equals(""))
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi/traffic.php?tid=" + id)

      if (viewSpot.getVisitGuide == null || viewSpot.getVisitGuide.equals(""))
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi/play.php?tid=" + id)

      if (viewSpot.getTips == null)
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi/tips.php?tid=" + id)
    }

    gen.writeEndObject()
  }

  // Polymorphic Single Bean Use
  override def serializeWithType(viewSpot: ViewSpot, gen: JsonGenerator, serializers: SerializerProvider, typeSer: TypeSerializer): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", viewSpot.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, viewSpot.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, viewSpot.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, viewSpot.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, viewSpot.address)

    gen.writeFieldName("style")
    gen.writeStartArray()
    val style = viewSpot.getStyle
    if (style != null) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      retLocality.serialize(style, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("images")
    val images = viewSpot.getImages
    gen.writeStartArray()
    if (images != null && !images.isEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeStringField("type", "vs") // 可以不需要了
    gen.writeStringField(AbstractPOI.FD_TIMECOSTDESC, viewSpot.timeCostDesc)
    gen.writeStringField(AbstractPOI.FD_PRICE_DESC, viewSpot.priceDesc)

    // Tel
    val tels = viewSpot.tel
    gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
    gen.writeStartArray()
    if (tels != null && (!tels.isEmpty)) {
      for (tel <- tels)
        gen.writeString(tel)
    }
    gen.writeEndArray()

    // Rank
    gen.writeNumberField(AbstractPOI.FD_RANK, checkRank(viewSpot.getRank.toInt))

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = viewSpot.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = viewSpot.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, viewSpot.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_DESC, viewSpot.desc)

      gen.writeStringField(ViewSpot.FD_OPEN_TIME, viewSpot.openTime)
      gen.writeStringField(ViewSpot.FD_TIME_COST_DESC, viewSpot.getTimeCostDesc)
      gen.writeStringField(ViewSpot.FD_TRAVEL_MONTH, viewSpot.getTravelMonth)
      if (viewSpot.getTrafficInfo == null || viewSpot.getTrafficInfo.equals(""))
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi/traffic.php?tid=" + id)

      if (viewSpot.getVisitGuide == null || viewSpot.getVisitGuide.equals(""))
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi/play.php?tid=" + id)

      if (viewSpot.getTips == null || viewSpot.getTips.equals(""))
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi/tips.php?tid=" + id)

      if (viewSpot.getDesc == null || viewSpot.getDesc.equals(""))
        gen.writeStringField("descUrl", "")
      else
        gen.writeStringField("descUrl", "http://h5.taozilvxing.com/poi/desc.php?tid=" + id)
    }
    gen.writeEndObject()
  }

  def checkRank(rank: Int): Int = {
    if (rank >= 999) 0 else rank
  }
}

object ViewSpotPOISerializerScala {

  def apply(): ViewSpotPOISerializerScala = {
    apply(Level.SIMPLE)
  }

  def apply(level: Level.Value): ViewSpotPOISerializerScala = {
    val result = new ViewSpotPOISerializerScala
    result.level = level
    result
  }
}