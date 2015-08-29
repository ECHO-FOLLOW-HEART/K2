package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.{ GeoJsonPoint, Locality, RmdLocality }
import models.misc.ImageItem
import utils.TaoziDataFilter
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class RmdLocalitySerializerScala extends JsonSerializer[RmdLocality] {
  override def serialize(rmdLocality: RmdLocality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", rmdLocality.getId.toString)
    gen.writeStringField(RmdLocality.FD_ZH_NAME, rmdLocality.getZhName)
    gen.writeStringField(RmdLocality.FD_EN_NAME, rmdLocality.getEnName)

    // location
    gen.writeFieldName(Locality.fnLocation)
    val geoJsonPoint = rmdLocality.getLocation
    val retLocalition = if (geoJsonPoint != null) serializers.findValueSerializer(classOf[GeoJsonPoint], null)
    else serializers.findNullValueSerializer(null)
    retLocalition.serialize(geoJsonPoint, gen, serializers)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(TaoziDataFilter.getOneImage(rmdLocality.getImages)) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()
    gen.writeEndObject()
  }
}
