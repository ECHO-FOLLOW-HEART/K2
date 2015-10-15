package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import formatter.taozi.geo.Level
import models.misc.{ ImageItem, TravelNote }
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class TravelNoteSerializerScala extends JsonSerializer[TravelNote] {

  var level: Level.Value = Level.SIMPLE

  override def serialize(travelNote: TravelNote, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", travelNote.getId.toString)
    gen.writeStringField(TravelNote.fnAuthorAvatar, travelNote.authorAvatar)
    gen.writeStringField(TravelNote.fnAuthorName, travelNote.authorName)
    gen.writeStringField(TravelNote.fnTitle, travelNote.title)
    gen.writeStringField(TravelNote.fnSummary, travelNote.summary)
    gen.writeBooleanField(TravelNote.fnEssence, travelNote.essence)

    if (travelNote.source != null &&
      (travelNote.source.contains("百度") || travelNote.source.contains("baidu")))
      gen.writeStringField(TravelNote.fnSource, "baidu")
    else
      gen.writeStringField(TravelNote.fnSource, "")

    // publishTime
    if (travelNote.publishTime == null)
      gen.writeNullField(TravelNote.fnPublishTime)
    else
      gen.writeNumberField(TravelNote.fnPublishTime, travelNote.publishTime)

    // travelTime
    if (travelNote.travelTime == null)
      gen.writeNullField(TravelNote.fnTravelTime)
    else
      gen.writeNumberField(TravelNote.fnTravelTime, travelNote.travelTime)

    // Images
    gen.writeFieldName("images")
    val images = travelNote.images
    val imagesOpt = Option(travelNote.images) map (_.toSeq)
    gen.writeStartArray()
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    if (level.equals(Level.DETAILED)) {
      gen.writeNumberField(TravelNote.fnRating, travelNote.rating)

      if (travelNote.upperCost == null)
        gen.writeNullField(TravelNote.fnUpperCost)
      else
        gen.writeNumberField(TravelNote.fnUpperCost, travelNote.upperCost)
      if (travelNote.lowerCost == null)
        gen.writeNullField(TravelNote.fnLowerCost)
      else
        gen.writeNumberField(TravelNote.fnLowerCost, travelNote.lowerCost)

      gen.writeNumberField(TravelNote.fnCommentCnt, travelNote.commentCnt)
      gen.writeNumberField(TravelNote.fnViewCnt, travelNote.viewCnt)
      gen.writeNumberField(TravelNote.fnFavorCnt, travelNote.favorCnt)

      // contents
      gen.writeFieldName(TravelNote.fnNoteContents)
      val contents = travelNote.contents
      gen.writeStartArray()
      if (contents != null && !contents.isEmpty) {
        for (cn <- contents) {
          gen.writeStartObject()
          for (entry <- cn.entrySet) {
            gen.writeStringField(entry.getKey, entry.getValue)
          }
          gen.writeEndObject()
        }
      }
      gen.writeEndArray()
    } else {
      // Travel detailed info
      gen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + travelNote.getId.toString)
    }

    gen.writeEndObject()
  }

}
