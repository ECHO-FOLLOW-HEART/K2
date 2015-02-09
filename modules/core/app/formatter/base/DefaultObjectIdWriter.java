package formatter.base;

import com.fasterxml.jackson.core.JsonGenerator;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Created by zephyre on 2/8/15.
 */
public class DefaultObjectIdWriter implements ObjectIdWriter {
    @Override
    public void write(ObjectId item, JsonGenerator jgen) throws IOException {
        jgen.writeStringField("id", item.toString());
    }
}
