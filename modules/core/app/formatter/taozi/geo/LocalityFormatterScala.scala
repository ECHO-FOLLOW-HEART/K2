package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.{ ObjectIdSerializer, BaseFormatter }
import formatter.taozi.ImageItemSerializerScala
import models.geo.Locality
import models.misc.ImageItem
import org.bson.types.ObjectId

/**
 * Created by pengyt on 2015/8/28.
 */
class LocalityFormatterScala extends BaseFormatter {

  var width = 0
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Locality], LocalitySerializerScala(Level.DETAILED))
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[ObjectId], new ObjectIdSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object LocalityFormatterScala {
  def apply(width: Int): LocalityFormatterScala = {
    val result = new LocalityFormatterScala
    result.width = width
    result
  }
}