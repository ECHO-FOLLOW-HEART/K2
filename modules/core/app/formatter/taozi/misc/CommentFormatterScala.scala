package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.misc.ImageItem
import models.poi.Comment

/**
 * Created by pengyt on 2015/8/31.
 */
class CommentFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Comment], new CommentSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala())
    mapper.registerModule(module)
    mapper
  }
}
