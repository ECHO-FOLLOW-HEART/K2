package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.misc.{ Album, ImageItem }

/**
 * Created by pengyt on 2015/8/31.
 */
class AlbumFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[Album], new AlbumSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}

object AlbumFormatterScala {

  def apply(): AlbumFormatterScala = {
    apply(640)
  }

  def apply(width: Int): AlbumFormatterScala = {
    val result = new AlbumFormatterScala
    result.width = width
    result
  }
}