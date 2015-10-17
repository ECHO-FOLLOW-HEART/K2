package formatter.taozi.poi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.taozi.SearchImageItemDeserializer
import models.misc.ImageItem
import models.poi.ViewSpot

/**
 * Created by pengyt on 2015/10/16.
 */
object SearchViewSpotParserScala {

  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[ImageItem], new SearchImageItemDeserializer())
    module.addDeserializer(classOf[ViewSpot], new SearchViewSpotDeserializerScala())
    mapper.registerModule(module)

    val result = mapper.readValue(contents, classOf[ViewSpot])
    result
  }
}
