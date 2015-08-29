package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.geo.{ RmdProvince, RmdLocality }
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/29.
 */
class RmdProvinceFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[RmdProvince], new RmdProvinceSerializerScala)
    module.addSerializer(classOf[RmdLocality], new RmdLocalitySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    mapper.registerModule(module)
    mapper
  }
}

object RmdProvinceFormatterScala {
  def apply(width: Int): RmdProvinceFormatterScala = {
    val result = new RmdProvinceFormatterScala
    result.width = width
    result
  }
}
