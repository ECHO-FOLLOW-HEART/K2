package formatter.taozi.guide

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.guide.ItinerItem
import models.poi.AbstractPOI

/**
 * Created by pengyt on 2015/8/29.
 */
class ItinerItemSerializerScala extends JsonSerializer[ItinerItem] {

  override def serialize(itinerItem: ItinerItem, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField(ItinerItem.fdDayIndex, itinerItem.dayIndex)

    gen.writeFieldName("poi")
    if (itinerItem.poi != null) {
      val retPOI = serializers.findValueSerializer(classOf[AbstractPOI], null)
      retPOI.serialize(itinerItem.poi, gen, serializers)
    }
    gen.writeEndObject()
  }
}
