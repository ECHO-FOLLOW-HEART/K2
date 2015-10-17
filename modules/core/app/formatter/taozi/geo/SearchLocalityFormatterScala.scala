package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import formatter.BaseFormatter
import formatter.taozi.SearchImageItemSerializerScala
import models.geo.Locality
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/10/15.
 */
class SearchLocalityFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Locality], new SearchLocalitySerializerScala)
    module.addSerializer(classOf[ImageItem], new SearchImageItemSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
