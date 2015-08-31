package formatter.taozi.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import models.misc.Recom

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * Created by pengyt on 2015/8/31.
 */
class RecomFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Recom], new RecomSerializerScala)
    mapper.registerModule(module)
    mapper
  }
}

