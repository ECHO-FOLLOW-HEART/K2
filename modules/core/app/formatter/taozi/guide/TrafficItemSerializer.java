package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.guide.ItinerItem;
import models.guide.TrafficItem;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class TrafficItemSerializer extends AizouSerializer<TrafficItem> {

    @Override
    public void serialize(TrafficItem itinerItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(ItinerItem.fdDayIndex,itinerItem.dayIndex);
        jsonGenerator.writeStringField("start", itinerItem.start);
        jsonGenerator.writeStringField("end", itinerItem.end);
        jsonGenerator.writeStringField("depTime", itinerItem.depTime);
        jsonGenerator.writeStringField("arrTime", itinerItem.arrTime);
        jsonGenerator.writeStringField("type", itinerItem.category);
        jsonGenerator.writeEndObject();
    }
}
