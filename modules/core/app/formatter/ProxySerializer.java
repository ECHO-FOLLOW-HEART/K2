package formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.Proxy;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zephyre on 1/15/15.
 */
public class ProxySerializer extends AizouSerializer<Proxy> {

    @Override
    public void serialize(Proxy proxy, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();

        jgen.writeStringField("scheme", getString(proxy.scheme));
        jgen.writeStringField("host", getString(proxy.host));
        jgen.writeNumberField("port", proxy.port);
        jgen.writeStringField("user", getString(proxy.user));
        jgen.writeStringField("passwd", getString(proxy.passwd));
        jgen.writeStringField("desc", getString(proxy.desc));

        jgen.writeObjectFieldStart("latency");
        if (proxy.latency != null) {
            for (Map.Entry<String, Float> items : proxy.latency.entrySet()) {
                jgen.writeNumberField(items.getKey(), items.getValue());
            }
        }
        jgen.writeEndObject();

        jgen.writeObjectFieldStart("verified");
        if (proxy.verified != null) {
            for (Map.Entry<String, Boolean> items : proxy.verified.entrySet()) {
                jgen.writeBooleanField(items.getKey(), items.getValue());
            }
        }
        jgen.writeEndObject();

        jgen.writeStringField("verifiedTime", getTimestamp(proxy.verifiedTime));

        jgen.writeEndObject();
    }
}
