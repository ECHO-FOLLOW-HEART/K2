package formatter.taozi.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.geo.{ LocalitySerializerScala, Level }
import models.geo.Locality
import models.user.UserInfo

/**
 * Created by pengyt on 2015/8/31.
 */
class UserInfoFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[UserInfo], UserInfoSerializerScala())
    module.addSerializer(classOf[Locality], LocalitySerializerScala(Level.FORTRACKS))
    mapper.registerModule(module)
    mapper
  }
}