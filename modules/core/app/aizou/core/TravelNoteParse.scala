package aizou.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.taozi.ImageItemDeserializer
import models.misc.{ ImageItem, TravelNoteScala }

/**
 * Created by pengyt on 2015/10/12.
 */
object TravelNoteParse {

  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[ImageItem], new ImageItemDeserializer())
    module.addDeserializer(classOf[TravelNoteScala], new TravelNoteDeserializer())
    mapper.registerModule(module)

    val result = mapper.readValue(contents, classOf[TravelNoteScala])
    result
  }
}
