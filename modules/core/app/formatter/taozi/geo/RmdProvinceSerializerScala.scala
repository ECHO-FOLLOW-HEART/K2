package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.{RmdProvince, RmdLocality}
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class RmdProvinceSerializerScala extends JsonSerializer[RmdProvince] {
  override def serialize(rmdProvince: RmdProvince, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", rmdProvince.getId.toString)
    gen.writeStringField(RmdProvince.FD_ZH_NAME, rmdProvince.getZhName)
    gen.writeStringField(RmdProvince.FD_EN_NAME, rmdProvince.getEnName)
    gen.writeStringField(RmdProvince.FD_EN_PINYIN, rmdProvince.getPinyin)

    gen.writeFieldName(RmdProvince.FD_EN_DESTINATION)

    val localityList = rmdProvince.getDestinations
    gen.writeStartArray()

    if (localityList != null && !localityList.isEmpty) {
      val ret = serializers.findValueSerializer(classOf[RmdLocality], null)
      for (rmdLocality <- localityList)
        ret.serialize(rmdLocality, gen, serializers)
    }

    gen.writeEndArray()
    gen.writeEndObject()
  }
}
