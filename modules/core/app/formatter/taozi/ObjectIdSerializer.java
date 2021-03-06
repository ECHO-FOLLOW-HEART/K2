package formatter.taozi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * 将ObjectId序列化为String
 *
 * @author Zephyre
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(objectId.toString());
    }
}
