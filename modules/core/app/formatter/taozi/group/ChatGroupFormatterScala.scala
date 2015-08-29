package formatter.taozi.group

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import models.group.ChatGroup

/**
 * Created by pengyt on 2015/8/29.
 */
class ChatGroupFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[ChatGroup], new ChatGroupSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}
