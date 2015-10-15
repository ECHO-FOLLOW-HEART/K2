package formatter.taozi

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{ JsonNode, DeserializationContext, JsonDeserializer, ObjectMapper }
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/10/13.
 */
class ImageItemDeserializer extends JsonDeserializer[ImageItem] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): ImageItem = {
    val node = (new ObjectMapper).readTree[JsonNode](p)
    val key = node.get("key").asText()
    val imageItem = new ImageItem()
    imageItem.setKey(key)
    imageItem
  }
}
