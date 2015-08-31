package formatter.taozi.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule

import formatter.BaseFormatter
import models.user.UserInfo

/**
 * Created by pengyt on 2015/8/31.
 */
class UserInfoSimpleFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[UserInfo], UserInfoSerializerScala())
    mapper.registerModule(module)
    mapper
  }
}