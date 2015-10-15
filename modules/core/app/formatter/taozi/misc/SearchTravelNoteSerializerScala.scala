package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.misc.{ TravelNoteScala, ImageItem }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class SearchTravelNoteSerializerScala extends JsonSerializer[TravelNoteScala] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(travelNote: TravelNoteScala, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", travelNote.getId.toString)
    gen.writeStringField(TravelNoteScala.fnAuthorAvatar, travelNote.authorAvatar)
    gen.writeStringField(TravelNoteScala.fnAuthorName, travelNote.authorName)
    gen.writeStringField(TravelNoteScala.fnTitle, travelNote.title)
    gen.writeStringField(TravelNoteScala.fnSummary, travelNote.summary)

    // publishTime
    if (travelNote.publishTime == 0)
      gen.writeNullField(TravelNoteScala.fnPublishTime)
    else
      gen.writeNumberField(TravelNoteScala.fnPublishTime, travelNote.publishTime)

    // Images
    gen.writeFieldName("images")
    val imagesOpt = Option(travelNote.images) map (_.toSeq)
    gen.writeStartArray()
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + travelNote.getId.toString)

    gen.writeEndObject()
  }
}
