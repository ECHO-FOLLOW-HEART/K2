package aizou.core

import com.fasterxml.jackson.core.{ JsonFactory, JsonParser }
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode }
import models.misc.{ ImageItem, TravelNoteScala }
import org.bson.types.ObjectId
import java.util.{ List => JList, ArrayList => JArrayList }
import scala.collection.JavaConversions._

/**
 * Created by zephyre on 7/4/15.
 */
class TravelNoteDeserializer extends JsonDeserializer[TravelNoteScala] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): TravelNoteScala = {
    val node = p.getCodec.readTree[JsonNode](p)

    val id = node.get("_id").asText()
    val authorAvatar = (node.get("authorAvatar") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val authorName = (node.get("authorName") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val title = (node.get("title") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val summary = (node.get("summary") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }) getOrElse ""

    val publishTime = (node.get("publishTime") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asLong())
    }) getOrElse 0L

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

    //    val detailUrl = (node.get("detailUrl") match {
    //      case _: NullNode => None
    //      case item: TextNode => Some(item.asText())
    //    }) getOrElse ""

    val travelNote = new TravelNoteScala()
    travelNote.id = new ObjectId(id)
    travelNote.authorAvatar = authorAvatar
    travelNote.authorName = authorName
    travelNote.title = title
    travelNote.summary = summary
    travelNote.publishTime = publishTime
    travelNote.images = seqAsJavaList(images)
    //    travelNote.detailUrl = detailUrl
    travelNote
  }
}
