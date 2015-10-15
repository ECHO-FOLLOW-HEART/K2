package formatter.taozi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/27.
 */
class ImageItemSerializerScala extends JsonSerializer[ImageItem] {

  var width: Int = 0

  var sizeDesc = ImageSizeDesc.MEDIUM

  override def serialize(imageItem: ImageItem, gen: JsonGenerator, serializers: SerializerProvider): Unit = {

    gen.writeStartObject()
    val maxWidth = if (width == 0) {
      if (sizeDesc == null) 960
      else sizeDesc match {
        case ImageSizeDesc.SMALL => 400
        case ImageSizeDesc.MEDIUM => 640
        case _ => 960
      }
    } else width

    // 如果只给url，则是原链接，图片没上传，无法裁剪缩放
    if (imageItem.getKey == null && imageItem.getUrl != null) {
      gen.writeStringField("url", imageItem.getUrl)
      gen.writeNumberField("width", null)
      gen.writeNumberField("height", null)
    } else {
      val fullUrl = imageItem.getFullUrl
      var widthValue = imageItem.getW
      var heightValue = imageItem.getH

      if (widthValue != null && heightValue != null) {
        val cropHint = imageItem.getCropHint
        val (imgUrl) = if (sizeDesc == ImageSizeDesc.FULL) fullUrl else {
          if (cropHint == null) {
            val r: Double = heightValue.toInt / widthValue.toInt
            widthValue = maxWidth
            heightValue = (widthValue.toInt * r).toInt
            f"$fullUrl%s?imageView2/2/w/$maxWidth%d"
          } else {
            val top = ImageItemSerializerScala.getCropHint(cropHint, "top")
            val right = ImageItemSerializerScala.getCropHint(cropHint, "right")
            val bottom = ImageItemSerializerScala.getCropHint(cropHint, "bottom")
            val left = ImageItemSerializerScala.getCropHint(cropHint, "left")
            widthValue = right - left
            heightValue = bottom - top
            f"$fullUrl%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!${widthValue.toInt}%dx${heightValue.toInt}%da$left%da$top%d/thumbnail/$maxWidth"
          }
        }

        gen.writeStringField("url", imgUrl)
        gen.writeNumberField("width", widthValue)
        gen.writeNumberField("height", heightValue)
      } else {
        gen.writeStringField("url", f"$fullUrl%s?imageView2/2/w/$maxWidth%d")
      }
    }
    gen.writeEndObject()
  }
}

object ImageItemSerializerScala {

  def apply(): ImageItemSerializerScala = {
    apply(ImageSizeDesc.MEDIUM)
  }

  def apply(sz: ImageSizeDesc.Value): ImageItemSerializerScala = {
    val result = new ImageItemSerializerScala
    result.sizeDesc = sz
    result.width = 0
    result
  }

  def apply(width: Int): ImageItemSerializerScala = {
    val result = new ImageItemSerializerScala
    result.width = width
    result
  }

  private def getCropHint(cropHint: java.util.Map[String, Integer], key: String): Int =
    if (cropHint.get(key) != null) cropHint.get(key).toInt else 0
}

object ImageSizeDesc extends Enumeration {
  // Value是一个类
  val SMALL, MEDIUM, LARGE, FULL = Value
}