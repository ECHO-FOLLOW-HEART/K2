package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.taozi.SearchImageItemDeserializer
import models.geo.Locality
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/10/15.
 */
object SearchLocalityParserScala {

  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[ImageItem], new SearchImageItemDeserializer())
    module.addDeserializer(classOf[Locality], new SearchLocalityDeserializerScala())
    mapper.registerModule(module)

    val result = mapper.readValue(contents, classOf[Locality])
    result
  }
}
