package formatter.taozi.poi

import java.util.{ ArrayList => JArrayList, List => JList }

import com.fasterxml.jackson.core.{ JsonFactory, JsonParser }
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode }
import models.misc.ImageItem
import models.poi.Shopping
import org.bson.types.ObjectId

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/10/15.
 */
class SearchShoppingDeserializerScala extends JsonDeserializer[Shopping] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Shopping = {
    val node = p.getCodec.readTree[JsonNode](p)

    val id = node.get("_id").asText()
    val zhName = (node.get("zhName") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val javaType = TypeFactory.defaultInstance().constructType(classOf[ImageItem])
    val deserializer = ctxt.findRootValueDeserializer(javaType)
    val jsonFactory = new JsonFactory
    val images = Option(node.get("images")) map (imageNode => {
      val ret: JList[ImageItem] = imageNode.iterator().toSeq map (node => {
        val parser = jsonFactory.createParser(node.toString)
        deserializer.deserialize(parser, ctxt).asInstanceOf[ImageItem]
      })
      ret
    }) getOrElse new JArrayList[ImageItem]

    val rating = (node.get("rating") match {
      case _: NullNode => None
      case item: DoubleNode => Some(item.asDouble())
    }) getOrElse 0.5

    val address = (node.get("address") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val style = (node.get("style") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val shopping = new Shopping()

    shopping.setId(new ObjectId(id))
    shopping.setZhName(zhName)
    shopping.setImages(seqAsJavaList(images))
    shopping.setRating(rating)
    shopping.setAddress(address)
    shopping.setStyle(style)
    shopping.setType("shopping")
    shopping
  }
}
