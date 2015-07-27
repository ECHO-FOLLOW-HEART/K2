package controllers.app

import aizou.core.PoiAPI
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.geo.DetailsEntryFormatter
import models.geo.{ DetailsEntry, Locality }
import play.api.mvc.{ Action, Controller }
import org.bson.types.ObjectId
import utils.Utils
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/7/27.
 */
object GeoCtrlScala extends Controller {
  def getTravelGuide(locId: String, field: Option[String]) = Action {
    request =>
      {
        if (field nonEmpty) {
          // 获取图片宽度
          val imgWidthStr = request.getQueryString("imgWidth")
          val imgWidth = if (imgWidthStr nonEmpty) imgWidthStr.get.toInt else 0
          val fieldList: java.util.List[String] = if (field nonEmpty) {
            field.get match {
              case "remoteTraffic" => seqAsJavaList(Seq(Locality.fnRemoteTraffic))
              case "localTraffic" => seqAsJavaList(Seq(Locality.fnLocalTraffic))
              case "activities" => seqAsJavaList(Seq(Locality.fnActivityIntro, Locality.fnActivities))
              case "tips" => seqAsJavaList(Seq(Locality.fnTips))
              case "specials" => seqAsJavaList(Seq(Locality.fnSpecials))
              case "geoHistory" => seqAsJavaList(Seq(Locality.fnGeoHistory))
              case "dining" => seqAsJavaList(Seq(Locality.fnDinningIntro, Locality.fnCuisines))
              case "shopping" => seqAsJavaList(Seq(Locality.fnShoppingIntro, Locality.fnCommodities))
              case "desc" => seqAsJavaList(Seq(Locality.fnDesc))
              case "diningTitles" => seqAsJavaList(Seq(Locality.fnCuisines))
              case "shoppingTitles" => seqAsJavaList(Seq(Locality.fnCommodities))
            }
          } else {
            //          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT").toScala
            seqAsJavaList(Seq())
          }
          val locality = PoiAPI.getLocalityByField(new ObjectId(locId), fieldList)
          if (locality == null) Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality is not exist.ID:" + locId).toScala
          val result = new ObjectMapper().createArrayNode()
          val detailsEntryFormatter = FormatterFactory.getInstance(classOf[DetailsEntryFormatter], imgWidth)
          if (field nonEmpty) {
            field.get match {
              case "remoteTraffic" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getRemoteTraffic() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getRemoteTraffic()))
                }
                result.add(contentsNode)
              }
              case "localTraffic" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getLocalTraffic() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getLocalTraffic()))
                }
                result.add(contentsNode)
              }
              case "activities" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", locality.getActivityIntro())
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getActivities() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getActivities()))
                }
                result.add(contentsNode)
              }
              case "tips" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getTips() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getTips()))
                }
                result.add(contentsNode)
              }
              case "geoHistory" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getGeoHistory() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getGeoHistory()))
                }
                result.add(contentsNode)
              }
              case "specials" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getSpecials() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getSpecials()))
                }
                result.add(contentsNode)
              }
              case "desc" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", "")
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getDesc() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().createObjectNode.put("contents", locality.getDesc())
                }
                result.add(contentsNode)
              }
              case "dining" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", locality.getDiningIntro())
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getCuisines() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getCuisines()))
                }
                result.add(contentsNode)
              }
              case "shopping" => {
                val descNode = new ObjectMapper().createObjectNode()
                descNode.put("desc", locality.getShoppingIntro())
                result.add(descNode)
                val contentsNode: JsonNode = if (locality.getCommodities() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(locality.getCommodities()))
                }
                result.add(contentsNode)
              }
              case "diningTitles" => {
                val contentsNode: JsonNode = if (locality.getSpecials() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(contentsTitles(locality.getCuisines()))
                }
                result.add(contentsNode)
              }
              case "shoppingTitles" => {
                val contentsNode: JsonNode = if (locality.getSpecials() != null) {
                  new ObjectMapper().createObjectNode()
                } else {
                  new ObjectMapper().valueToTree(contentsTitles(locality.getCommodities()))
                }
                result.add(contentsNode)
              }
            }
          }
          Utils.createResponse(ErrorCode.NORMAL, result).toScala
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT")
        }
        null
      }
  }

  def contentsTitles(entries: java.util.List[DetailsEntry]) {
    if (entries == null) ""
    else {
      val sb = new StringBuilder()
      for (entry <- entries) {
        sb.append(entry.getTitle())
        sb.append(" ")
      }
      sb.toString()
    }
  }
}
