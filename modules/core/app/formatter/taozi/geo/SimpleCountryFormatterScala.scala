package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.geo.{Continent, Country}
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleCountryFormatterScala extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Country], new SimpleCountrySerializerScala)
    module.addSerializer(classOf[Continent], new SimpleContinentSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala())
    mapper.registerModule(module)
    mapper
  }
}
