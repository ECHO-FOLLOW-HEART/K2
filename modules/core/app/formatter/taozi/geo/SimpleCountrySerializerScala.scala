package formatter.taozi.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.{ Continent, Country }
import models.misc.ImageItem
import org.bson.types.ObjectId
import utils.TaoziDataFilter
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/29.
 */
class SimpleCountrySerializerScala extends JsonSerializer[Country] {

  override def serialize(country: Country, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", country.getId.toString)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()

    val imagesOpt = Option(TaoziDataFilter.getOneImage(country.getImages)) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeStringField("zhName", country.getZhName)
    gen.writeStringField("enName", country.getEnName)
    gen.writeStringField("code", country.getCode)

    gen.writeFieldName("continents")
    val code = country.getContCode
    val zhCont = country.getZhCont
    val enCont = country.getEnCont
    val continent = new Continent
    continent.setId(new ObjectId)
    continent.setCode(code)
    continent.setZhName(zhCont)
    continent.setEnName(enCont)
    val retContinent = if (code != null && zhCont != null && enCont != null)
      serializers.findValueSerializer(classOf[Continent], null)
    else serializers.findNullValueSerializer(null)
    retContinent.serialize(continent, gen, serializers)

    gen.writeEndObject()
  }
}
