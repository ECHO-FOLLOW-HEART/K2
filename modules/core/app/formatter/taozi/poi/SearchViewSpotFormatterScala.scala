package formatter.taozi.poi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import formatter.BaseFormatter
import formatter.taozi.SearchImageItemSerializerScala
import models.misc.ImageItem
import models.poi.ViewSpot

/**
 * Created by pengyt on 2015/10/16.
 */
class SearchViewSpotFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[ViewSpot], new SearchViewSpotSerializerScala)
    module.addSerializer(classOf[ImageItem], new SearchImageItemSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
