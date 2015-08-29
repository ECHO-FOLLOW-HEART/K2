package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import models.geo.Continent

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleContinentFormatterScala extends BaseFormatter{
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Continent], new SimpleContinentSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
