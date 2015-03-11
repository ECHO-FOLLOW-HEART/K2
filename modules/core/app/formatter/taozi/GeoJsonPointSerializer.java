package formatter.taozi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.guide.Guide;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Restaurant;
import models.poi.Shopping;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;

/**
 * Created by zephyre on 12/6/14.
 */
public class GeoJsonPointSerializer extends AizouSerializer<GeoJsonPoint> {

    @Override
    public void serialize(GeoJsonPoint localition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        // Location
        jsonGenerator.writeFieldName(GeoJsonPoint.FD_COORDS);
        jsonGenerator.writeStartArray();
        if (localition != null && localition.getCoordinates() != null && localition.getCoordinates().length >= 2) {
            jsonGenerator.writeNumber(localition.getCoordinates()[0]);
            jsonGenerator.writeNumber(localition.getCoordinates()[1]);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
