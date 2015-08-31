package formatter.taozi.poi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import models.poi.AbstractPOI

/**
 * Created by pengyt on 2015/8/31.
 */
class BriefPOIFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[AbstractPOI], new BriefPOISerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
