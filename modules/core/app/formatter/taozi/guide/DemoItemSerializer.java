package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.guide.DemoItem;
import models.guide.ItinerItem;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class DemoItemSerializer extends AizouSerializer<DemoItem> {

    @Override
    public void serialize(DemoItem itinerItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(ItinerItem.fdDayIndex, getValue(itinerItem.dayIndex));
        jsonGenerator.writeStringField("desc", itinerItem.desc);
        jsonGenerator.writeEndObject();
    }
}
