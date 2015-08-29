package formatter.taozi.guide

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.{ GeoJsonPointSerializerScala, ImageItemSerializerScala }
import formatter.taozi.geo.LocalitySerializerScala
import formatter.taozi.poi.POISerializerScala
import models.geo.{ GeoJsonPoint, Locality }
import models.guide.{ ItinerItem, Guide }
import models.misc.ImageItem
import models.poi.AbstractPOI

/**
 * Created by pengyt on 2015/8/29.
 */
class GuideFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Guide], new GuideSerializerScala)
    module.addSerializer(classOf[Locality], new LocalitySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[GeoJsonPoint], new GeoJsonPointSerializerScala)
    module.addSerializer(classOf[AbstractPOI], new POISerializerScala)
    module.addSerializer(classOf[ItinerItem], new ItinerItemSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}

object GuideFormatterScala {
  def apply(width: Int): GuideFormatterScala = {
    val result = new GuideFormatterScala
    result.width = width
    result
  }
}