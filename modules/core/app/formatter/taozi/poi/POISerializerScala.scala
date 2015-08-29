package formatter.taozi.poi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.AizouBaseEntity
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import models.poi.{ Shopping, Restaurant, ViewSpot, AbstractPOI }
import org.bson.types.ObjectId
import utils.TaoziDataFilter
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class POISerializerScala extends JsonSerializer[AbstractPOI] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(abstractPOI: AbstractPOI, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", abstractPOI.getId.toString)
    gen.writeStringField(AbstractPOI.FD_ZH_NAME, abstractPOI.zhName)
    gen.writeStringField(AbstractPOI.FD_EN_NAME, abstractPOI.enName)
    gen.writeNumberField(AbstractPOI.FD_RATING, abstractPOI.rating)
    gen.writeStringField(AbstractPOI.FD_ADDRESS, abstractPOI.address)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(abstractPOI.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    // Diff POI
    abstractPOI match {
      case viewSpot: ViewSpot =>
        // Type use for serialize
        gen.writeStringField("type", "vs")
        // TimeCost
        gen.writeStringField(AbstractPOI.FD_TIMECOSTDESC, abstractPOI.timeCostDesc)
        // PriceDesc
        gen.writeStringField(AbstractPOI.FD_PRICE_DESC, abstractPOI.priceDesc)
      case restaurant: Restaurant =>
        // Type use for serialize
        gen.writeStringField("type", "restaurant")
        // PriceDesc
        gen.writeStringField(AbstractPOI.FD_PRICE_DESC, abstractPOI.priceDesc)
        // Tel
        val tels = abstractPOI.tel
        gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
        gen.writeStartArray()
        if (tels != null && (!tels.isEmpty)) {
          for (tel <- tels)
            gen.writeString(tel)
        }
        gen.writeEndArray()
      case shopping: Shopping =>
        // Type use for serialize
        gen.writeStringField("type", "shopping")
        // Tel
        val tels = abstractPOI.tel
        gen.writeFieldName(AbstractPOI.FD_TELEPHONE)
        gen.writeStartArray()
        if (tels != null && (!tels.isEmpty)) {
          for (tel <- tels)
            gen.writeString(tel)
        }
        gen.writeEndArray()
    }

    // Rank
    val rank = abstractPOI.getRank
    gen.writeNumberField(AbstractPOI.FD_RANK, if (rank != null) rank.toInt else 999)

    // Targets
    gen.writeFieldName(AbstractPOI.detTargets)
    val targets = abstractPOI.targets
    if (targets != null && !targets.isEmpty) {
      gen.writeStartArray()
      val retObjectId = serializers.findValueSerializer(classOf[ObjectId], null)
      for (id <- targets)
        retObjectId.serialize(id, gen, serializers)
      gen.writeEndArray()
    } else {
      val retObjectId = serializers.findNullValueSerializer(null)
      retObjectId.serialize(targets, gen, serializers)
    }

    // Locality
    gen.writeFieldName(AbstractPOI.FD_LOCALITY)
    val localities = abstractPOI.getLocality
    val retLocality = if (localities != null) serializers.findValueSerializer(classOf[Locality], null)
    else serializers.findNullValueSerializer(null)
    retLocality.serialize(localities, gen, serializers)

    // Location
    gen.writeFieldName(AbstractPOI.FD_LOCATION)
    val geoJsonPoint = abstractPOI.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    if (level.equals(Level.DETAILED)) {
      val id = abstractPOI.getId.toString
      gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, abstractPOI.getIsFavorite)
      gen.writeStringField(AbstractPOI.FD_PRICE_DESC, TaoziDataFilter.getPriceDesc(abstractPOI))

      if (abstractPOI.getTrafficInfo == null || abstractPOI.getTrafficInfo.equals(""))
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi_traffic.php?tid=" + id)

      if (abstractPOI.getVisitGuide == null || abstractPOI.getVisitGuide.equals(""))
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi_play.php?tid=" + id)

      if (abstractPOI.getTips == null)
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "")
      else
        gen.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi_tips.php?tid=" + id)
    }
    gen.writeEndObject()
  }
}

object POISerializerScala {

  def apply(): POISerializerScala = {
    apply(Level.SIMPLE)
  }

  def apply(level: Level.Value): POISerializerScala = {
    val result = new POISerializerScala
    result.level = level
    result
  }
}