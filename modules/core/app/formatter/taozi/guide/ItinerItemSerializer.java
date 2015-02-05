package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.guide.ItinerItem;
import models.poi.AbstractPOI;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class ItinerItemSerializer extends AizouSerializer<ItinerItem> {

    @Override
    public void serialize(ItinerItem itinerItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(ItinerItem.fdDayIndex, getValue(itinerItem.dayIndex));

        jsonGenerator.writeFieldName("poi");
        if (itinerItem.poi != null) {
            JsonSerializer<Object> retPOI = serializerProvider.findValueSerializer(AbstractPOI.class, null);
            retPOI.serialize(itinerItem.poi, jsonGenerator, serializerProvider);
        } else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndObject();

    }
}
