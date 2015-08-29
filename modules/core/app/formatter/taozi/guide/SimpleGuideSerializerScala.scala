package formatter.taozi.guide

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.guide.{ AbstractGuide, Guide }
import models.misc.ImageItem

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */

class SimpleGuideSerializerScala extends JsonSerializer[Guide] {

  override def serialize(guide: Guide, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", guide.getId.toString)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(guide.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeStringField(AbstractGuide.fnTitle, guide.title)
    gen.writeStringField(Guide.fnStatus, if (guide.getStatus == null) Guide.fnStatusPlanned else guide.getStatus)
    gen.writeNumberField(Guide.fnDayCnt, guide.getDayCnt.toInt)
    gen.writeStringField(Guide.fnSummary, guide.getSummary)
    gen.writeNumberField(Guide.fnUpdateTime, guide.getUpdateTime)

    gen.writeEndObject()
  }
}

