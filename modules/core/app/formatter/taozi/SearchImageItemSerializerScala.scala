package formatter.taozi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/27.
 */
class SearchImageItemSerializerScala extends JsonSerializer[ImageItem] {

  override def serialize(imageItem: ImageItem, gen: JsonGenerator, serializers: SerializerProvider): Unit = {

    gen.writeStartObject()
    if (imageItem.getKey != null) gen.writeStringField("key", imageItem.getKey)
    else gen.writeStringField("key", "")
    gen.writeEndObject()
  }
}