package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.{ ObjectIdSerializer, BaseFormatter }
import formatter.taozi.ImageItemSerializerScala
import models.geo.DetailsEntry
import models.misc.ImageItem
import org.bson.types.ObjectId

/**
 * Created by pengyt on 2015/8/28.
 */
class DetailsEntryFormatterScala extends BaseFormatter {

  var width = 0

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[DetailsEntry], new DetailsEntrySerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala(width))
    module.addSerializer(classOf[ObjectId], new ObjectIdSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object DetailsEntryFormatterScala {
  def apply(width: Int): DetailsEntryFormatterScala = {
    val result = new DetailsEntryFormatterScala
    result.width = width
    result
  }
}