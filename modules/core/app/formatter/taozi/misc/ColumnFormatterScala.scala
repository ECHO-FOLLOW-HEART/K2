package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import models.misc.Column

/**
 * Created by pengyt on 2015/8/31.
 */
class ColumnFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Column], new ColumnSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
