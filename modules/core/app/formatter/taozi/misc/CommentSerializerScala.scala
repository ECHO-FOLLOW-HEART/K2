package formatter.taozi.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem
import models.poi.Comment
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/31.
 */
class CommentSerializerScala extends JsonSerializer[Comment] {

  override def serialize(comment: Comment, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject();
    gen.writeStringField("id", comment.getId.toString)

    // images
    gen.writeFieldName(Comment.FD_IMAGES)
    gen.writeStartArray()

    val imagesOpt = Option(comment.getImages) map (_.toSeq)
    if (imagesOpt nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- imagesOpt.get)
        ret.serialize(image, gen, serializers)
    }

    gen.writeEndArray()

    if (comment.getUserId == null)
      gen.writeNullField(Comment.FD_USER_ID)
    else
      gen.writeNumberField(Comment.FD_USER_ID, comment.getUserId)

    gen.writeStringField(Comment.FD_AVATAR, comment.getAuthorAvatar)
    gen.writeStringField(Comment.FD_AUTHOR_NAME, comment.getAuthorName)
    gen.writeStringField(Comment.FD_CONTENTS, comment.getContents)
    gen.writeNumberField(Comment.FD_RATING, comment.getRating)
    gen.writeNumberField(Comment.FD_PUBLISHTIME, comment.getPublishTime)

    gen.writeEndObject()
  }
}
