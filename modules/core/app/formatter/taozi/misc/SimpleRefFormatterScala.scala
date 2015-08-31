package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.geo.LocalitySerializerScala
import models.geo.Locality
import models.misc.SimpleRef

/**
 * Created by pengyt on 2015/8/31.
 */
class SimpleRefFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Locality], new LocalitySerializerScala)
    module.addSerializer(classOf[SimpleRef], new SimpleRefSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
