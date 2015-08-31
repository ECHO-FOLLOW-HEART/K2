package formatter.taozi.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.user.UserInfo
import utils.TaoziDataFilter
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class UserInfoSerializerScala extends JsonSerializer[UserInfo] {

  var selfView: Boolean = false

  override def serialize(userInfo: UserInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", userInfo.getId.toString)
    gen.writeStringField("nickName", userInfo.getNickName)
    gen.writeStringField("avatar", userInfo.getAvatar)
    gen.writeStringField("avatarSmall", userInfo.getAvatarSmall)
    gen.writeStringField("gender", userInfo.getGender)
    gen.writeStringField("signature", userInfo.getSignature)
    gen.writeNumberField("userId", userInfo.getUserId)
    gen.writeBooleanField("isBlocked", false)

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
    gen.writeStringField(UserInfo.fnTravelStatus, userInfo.getTravelStatus)
    gen.writeStringField(UserInfo.fnResidence, userInfo.getResidence)
    gen.writeStringField(UserInfo.fnBirthday, userInfo.getBirthday.toString)
    gen.writeStringField(UserInfo.fnZodiac, TaoziDataFilter.getZodiac(userInfo.getZodiac))

    if (selfView) {
      gen.writeStringField("tel", userInfo.getTel)
      gen.writeNumberField("dialCode", userInfo.getDialCode.toInt)
    } else
      gen.writeStringField("memo", userInfo.getMemo)
    gen.writeEndObject()
  }

}

object UserInfoSerializerScala {

  def apply(): UserInfoSerializerScala = {
    apply(false)
  }

  def apply(selfView: Boolean): UserInfoSerializerScala = {
    val result = new UserInfoSerializerScala
    result.selfView = selfView
    result
  }
}
