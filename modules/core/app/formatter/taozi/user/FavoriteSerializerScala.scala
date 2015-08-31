package formatter.taozi.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.geo.Locality
import models.misc.ImageItem
import models.user.Favorite
import org.apache.commons.lang3.StringUtils
import org.bson.types.ObjectId
import utils.Constants
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class FavoriteSerializerScala extends JsonSerializer[Favorite] {

  override def serialize(favorite: Favorite, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", favorite.getId.toString)
    val itemId = favorite.itemId
    if (itemId != null) {
      val ret = serializers.findValueSerializer(classOf[ObjectId], null)
      gen.writeFieldName(Favorite.fnItemId)
      ret.serialize(itemId, gen, serializers)
    } else {
      gen.writeStringField(Favorite.fnItemId, "")
    }

    gen.writeStringField(Favorite.fnZhName, favorite.zhName)
    gen.writeStringField(Favorite.fnEnName, favorite.enName)
    gen.writeStringField(Favorite.fnType, favorite.`type`)
    gen.writeStringField(Favorite.fnDesc, StringUtils.abbreviate(favorite.desc, Constants.ABBREVIATE_LEN))
    gen.writeNumberField(Favorite.fnCreateTime, favorite.createTime.getTime)
    gen.writeNumberField(Favorite.fnUserId, favorite.userId)

    // Images
    gen.writeFieldName("images")
    val images = Option(favorite.images) map (_.toSeq)
    gen.writeStartArray()
    if (images nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images.get)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    // Locality
    gen.writeFieldName(Favorite.fnLocality)
    val locality = favorite.locality
    if (locality != null) {
      val retLocality = serializers.findValueSerializer(classOf[Locality], null)
      retLocality.serialize(locality, gen, serializers)
    } else {
      gen.writeStartObject()
      gen.writeEndObject()
    }

    val type1 = favorite.`type`
    if (type1.equals("vs") || type1.equals("locality")) {
      gen.writeStringField(Favorite.fnTimeCostDesc, favorite.timeCostDesc)
    } else if (type1.equals("restaurant") || type1.equals("hotel")) {
      gen.writeStringField(Favorite.fnTimeCostDesc, favorite.timeCostDesc)
      gen.writeStringField(Favorite.fnAddress, favorite.address)
      gen.writeStringField(Favorite.fnPriceDesc, favorite.priceDesc)
      gen.writeStringField(Favorite.fnTelephone, favorite.telephone)
      gen.writeNumberField(Favorite.fnRating, favorite.rating)
    } else if (type1.equals("shopping")) {
      gen.writeNumberField(Favorite.fnRating, favorite.rating)
    } else if (type1.equals("travelNote"))
      gen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + favorite.itemId.toString)

    gen.writeEndObject()
  }

}
