package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.Column

/**
 * Created by pengyt on 2015/8/31.
 */
class ColumnSerializerScala extends JsonSerializer[Column] {

  override def serialize(column: Column, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", column.getId.toString)
    gen.writeStringField(Column.FD_COVER, column.getCover)
    gen.writeStringField(Column.FD_LINK, column.getLink)
    gen.writeStringField(Column.FD_TITLE, column.getTitle)
    gen.writeStringField(Column.FD_TYPE, column.getType)

    // IOS不支持tp=webp格式的页面
    val content = column.getContent.replaceAll("\\?tp=webp&", "\\?")
    gen.writeStringField(Column.FD_CONTENT, content)
    gen.writeEndObject()
  }
}
