package formatter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.Proxy

/**
 * Created by pengyt on 2015/8/31.
 */
class ProxySerializerScala extends JsonSerializer[Proxy] {

  override def serialize(proxy: Proxy, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("scheme", proxy.scheme)
    gen.writeStringField("host", proxy.host)
    gen.writeNumberField("port", proxy.port)
    gen.writeStringField("user", proxy.user)
    gen.writeStringField("passwd", proxy.passwd)
    gen.writeStringField("desc", proxy.desc)

    gen.writeObjectFieldStart("latency")

    //    if (proxy.latency != null) {
    //      for (items <- proxy.latency.entrySet) {
    //        // gen.writeNumberField(items.getKey, items.getValue)
    //      }
    //    }
    //    gen.writeEndObject()
    //
    //    gen.writeObjectFieldStart("verified")
    //    if (proxy.verified != null) {
    //      for (items <- proxy.verified.entrySet()) {
    //        //gen.writeBooleanField(items.getKey, items.getValue)
    //      }
    //    }
    gen.writeEndObject()

    gen.writeStringField("verifiedTime", proxy.verifiedTime.toString)

    gen.writeEndObject()
  }

}
