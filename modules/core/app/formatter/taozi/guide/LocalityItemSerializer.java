package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Locality;
import models.guide.ItinerItem;
import models.guide.LocalityItem;

import java.io.IOException;

/**
 * Created by zephyre on 12/6/14.
 */
public class LocalityItemSerializer extends AizouSerializer<LocalityItem> {

    @Override
    public void serialize(LocalityItem itinerItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(ItinerItem.fdDayIndex, getValue(itinerItem.dayIndex));

        jsonGenerator.writeFieldName("locality");
        if (itinerItem.locality != null) {
            JsonSerializer<Object> retPOI = serializerProvider.findValueSerializer(Locality.class, null);
            retPOI.serialize(itinerItem.locality, jsonGenerator, serializerProvider);
        } else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndObject();
    }
}
