package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.AizouBaseEntity
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/28.
 */
class LocalitySerializerScala extends JsonSerializer[Locality] {

  var level: Level.Value = Level.SIMPLE
  override def serialize(locality: Locality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", locality.getId.toString)
    gen.writeStringField("zhName", locality.getZhName)
    gen.writeStringField("enName", locality.getEnName)

    level match {
      case Level.DETAILED =>
        gen.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, locality.getIsFavorite)
        gen.writeStringField(Locality.fnDesc, locality.getDesc)
        gen.writeStringField(Locality.fnTimeCostDesc, locality.getTimeCostDesc)
        gen.writeStringField(Locality.fnTravelMonth, locality.getTravelMonth)
        gen.writeNumberField(Locality.fnImageCnt, if (locality.getImages == null) 0 else locality.getImages.size)
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
    }

    // location
    gen.writeFieldName(Locality.fnLocation)
    val geoJsonPoint = locality.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    gen.writeEndObject()
  }
}

object LocalitySerializerScala {

  def apply(): LocalitySerializerScala = {
    apply(Level.SIMPLE)
  }

  def apply(level: Level.Value): LocalitySerializerScala = {
    val result = new LocalitySerializerScala
    result.level = level
    result
  }
}

object Level extends Enumeration {
  // Value是一个类
  val SIMPLE, DETAILED, FORTRACKS = Value
}