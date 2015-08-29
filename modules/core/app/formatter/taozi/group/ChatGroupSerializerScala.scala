package formatter.taozi.group

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.group.ChatGroup

/**
 * Created by pengyt on 2015/8/29.
 */
class ChatGroupSerializerScala extends JsonSerializer[ChatGroup] {

  override def serialize(chatGroup: ChatGroup, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField(ChatGroup.FD_GROUPID, chatGroup.getGroupId)
    gen.writeNumberField(ChatGroup.FD_CREATOR, chatGroup.getCreator)
    gen.writeStringField(ChatGroup.FD_NAME, chatGroup.getName)
    gen.writeStringField(ChatGroup.FD_AVATAR, chatGroup.getAvatar)
    gen.writeStringField(ChatGroup.FD_DESC, chatGroup.getDesc)
    gen.writeNumberField(ChatGroup.FD_MAXUSERS, chatGroup.getMaxUsers.toInt)
    gen.writeEndObject()
  }
}
