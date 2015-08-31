package formatter.taozi.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import formatter.taozi.geo.LocalitySerializerScala
import models.geo.Locality
import models.misc.ImageItem
import models.user.Favorite

/**
 * Created by pengyt on 2015/8/31.
 */
class FavoriteFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Favorite], new FavoriteSerializerScala)
    module.addSerializer(classOf[Locality], new LocalitySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object FavoriteFormatterScala {

  def apply(width: Int): FavoriteFormatterScala = {
    val result = new FavoriteFormatterScala
    result.width = width
    result
  }
}