package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.geo.{Country, Locality}
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleLocalityWithLocationFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Locality], new SimpleLocalityWithLocationSerializerScala)
    module.addSerializer(classOf[Country], new SimpleCountrySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object SimpleLocalityWithLocationFormatterScala {
  def apply(width: Int): SimpleLocalityWithLocationFormatterScala = {
    val result = new SimpleLocalityWithLocationFormatterScala
    result.width = width
    result
  }
}
