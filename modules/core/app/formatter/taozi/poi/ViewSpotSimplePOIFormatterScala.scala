package formatter.taozi.poi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.{ GeoJsonPointSerializerScala, ImageItemSerializerScala }
import formatter.taozi.geo.LocalitySerializerScala
import models.geo.{ GeoJsonPoint, Locality }
import models.misc.ImageItem
import models.poi.ViewSpot

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * Created by pengyt on 2015/8/31.
 */
class ViewSpotSimplePOIFormatterScala extends BaseFormatter {

  var width: Int = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[Locality], new LocalitySerializerScala)
    module.addSerializer(classOf[GeoJsonPoint], new GeoJsonPointSerializerScala)
    module.addSerializer(classOf[ViewSpot], new ViewSpotPOISerializerScala)
    mapper.registerModule(module)
    mapper
  }

}

object ViewSpotSimplePOIFormatterScala {

  def apply(width: Int): ViewSpotSimplePOIFormatterScala = {
    val result = new ViewSpotSimplePOIFormatterScala
    result.width = width
    result
  }
}