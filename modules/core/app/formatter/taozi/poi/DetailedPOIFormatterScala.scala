package formatter.taozi.poi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.{ GeoJsonPointSerializerScala, ImageItemSerializerScala }
import formatter.taozi.geo.{ Level, LocalitySerializerScala }
import formatter.taozi.guide.ItinerItemSerializerScala
import models.geo.{ GeoJsonPoint, Locality }
import models.guide.ItinerItem
import models.misc.ImageItem
import models.poi._

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * Created by pengyt on 2015/8/31.
 */
class DetailedPOIFormatterScala extends BaseFormatter {

  var width: Int = 0

  var level: Level.Value = Level.DETAILED

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[ViewSpot], ViewSpotPOISerializerScala(Level.DETAILED))
    module.addSerializer(classOf[Hotel], HotelPOISerializerScala(Level.DETAILED))
    module.addSerializer(classOf[Restaurant], RestaurantPOISerializerScala(Level.DETAILED))
    module.addSerializer(classOf[Shopping], ShoppingPOISerializerScala(Level.DETAILED))
    module.addSerializer(classOf[Locality], new LocalitySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[ItinerItem], new ItinerItemSerializerScala)
    module.addSerializer(classOf[GeoJsonPoint], new GeoJsonPointSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}

object DetailedPOIFormatterScala {

  def apply(width: Int): DetailedPOIFormatterScala = {
    val result = new DetailedPOIFormatterScala
    result.width = width
    result
  }
}