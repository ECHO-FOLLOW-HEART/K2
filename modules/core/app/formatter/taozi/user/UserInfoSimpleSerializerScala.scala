package formatter.taozi.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.user.UserInfo
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class UserInfoSimpleSerializerScala extends JsonSerializer[UserInfo] {

  override def serialize(userInfo: UserInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("nickName", userInfo.getNickName)
    gen.writeStringField("avatar", userInfo.getAvatar)
    gen.writeStringField("avatarSmall", userInfo.getAvatarSmall)
    gen.writeStringField("gender", userInfo.getGender)
    gen.writeStringField("signature", userInfo.getSignature)
    gen.writeNumberField("userId", userInfo.getUserId)

    // roles
    gen.writeFieldName(UserInfo.fnRoles)
    gen.writeStartArray()
    val roles = Option(userInfo.getRoles) map (_.toSeq)
    if (roles nonEmpty) {
      val retLocality = serializers.findValueSerializer(classOf[String], null)
      for (role <- roles.get) {
        retLocality.serialize(role, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeNumberField(UserInfo.fnLevel, userInfo.getLevel)
    gen.writeStringField(UserInfo.fnResidence, userInfo.getResidence)
    gen.writeStringField(UserInfo.fnBirthday, userInfo.getBirthday.toString)

    gen.writeEndObject()
  }

}
