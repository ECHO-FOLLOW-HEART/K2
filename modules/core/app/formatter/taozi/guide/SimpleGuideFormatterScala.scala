package formatter.taozi.guide

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.guide.Guide
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleGuideFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Guide], new SimpleGuideSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object SimpleGuideFormatterScala {
  def apply(width: Int): SimpleGuideFormatterScala = {
    val result = new SimpleGuideFormatterScala
    result.width = width
    result
  }
}