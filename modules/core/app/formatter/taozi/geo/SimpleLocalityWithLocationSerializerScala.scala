package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}
import models.geo.{Country, GeoJsonPoint, Locality}
import models.misc.ImageItem
import utils.TaoziDataFilter

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleLocalityWithLocationSerializerScala extends JsonSerializer[Locality]{

  override def serialize(locality: Locality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", locality.getId.toString)
    gen.writeStringField("zhName", locality.getZhName)
    gen.writeStringField("enName", locality.getEnName)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(TaoziDataFilter.getOneImage(locality.getImages)) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    // location
    gen.writeFieldName(Locality.fnLocation)
    val geoJsonPoint = locality.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    // Country
    gen.writeFieldName(Locality.fnCountry)
    val country = locality.getCountry()
    val retCountry = if (country != null) serializers.findValueSerializer(classOf[Country], null)
      else serializers.findNullValueSerializer(null)
    retCountry.serialize(country, gen, serializers)

    gen.writeEndObject()
  }
}