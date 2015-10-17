package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.taozi.SearchImageItemDeserializer
import models.misc.{ ImageItem, TravelNoteScala }

/**
 * Created by pengyt on 2015/10/12.
 */
object SearchTravelNoteParserScala {

  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[ImageItem], new SearchImageItemDeserializer())
    module.addDeserializer(classOf[TravelNoteScala], new SearchTravelNoteDeserializerScala())
    mapper.registerModule(module)

    val result = mapper.readValue(contents, classOf[TravelNoteScala])
    result
  }
}
