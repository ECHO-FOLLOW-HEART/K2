package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.{ Continent, CountryExpert }
import models.misc.ImageItem
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/27.
 */
class CountryExpertSerializerScala extends JsonSerializer[CountryExpert] {

  // 是否需要抛IOException?
  override def serialize(countryExpert: CountryExpert, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", countryExpert.getId.toString)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(countryExpert.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeStringField("zhName", countryExpert.getZhName)
    gen.writeStringField("enName", countryExpert.getEnName)
    gen.writeStringField("code", countryExpert.getCode)
    gen.writeNumberField("rank", if (countryExpert.getRank == null) 100 else countryExpert.getRank.toInt)
    gen.writeNumberField("expertCnt", countryExpert.getExpertCnt.toInt)

    gen.writeFieldName("continents")
    val continent = countryExpert.getContinent
    val retContinent = if (continent != null) serializers.findValueSerializer(classOf[Continent], null)
    else serializers.findNullValueSerializer(null)
    retContinent.serialize(continent, gen, serializers)

    gen.writeEndObject()
  }
}
