package formatter.taozi.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.misc.{ Track, ImageItem }

/**
 * Created by pengyt on 2015/8/31.
 */
class TrackFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Track], new TrackSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object TrackFormatterScala {

  def apply(width: Int): TrackFormatterScala = {
    val result = new TrackFormatterScala
    result.width = width
    result
  }
}