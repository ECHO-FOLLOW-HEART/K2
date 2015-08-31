package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.misc.{ TravelNote, ImageItem }

/**
 * Created by pengyt on 2015/8/31.
 */
class TravelNoteFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[TravelNote], new TravelNoteSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object TravelNoteFormatterScala {

  def apply(): TravelNoteFormatterScala = {
    apply(640)
  }

  def apply(width: Int): TravelNoteFormatterScala = {
    val result = new TravelNoteFormatterScala
    result.width = width
    result
  }
}