package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.{ ImageItem, Album }

/**
 * Created by pengyt on 2015/8/31.
 */
class AlbumSerializerScala extends JsonSerializer[Album] {
  override def serialize(album: Album, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", album.getId.toString)

    // images
    gen.writeFieldName(Album.FD_IMAGE)
    gen.writeStartArray()

    val imagesOpt = Option(Seq(album.getImage))
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    gen.writeNumberField(Album.FD_CTIME, album.getcTime)

    gen.writeEndObject()
  }
}
