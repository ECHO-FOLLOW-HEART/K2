package controllers.app

import aizou.core.{ PoiAPI, MiscAPI }
import aspectj.Key
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.fasterxml.jackson.databind.node.ObjectNode
import exception.{ AizouException, ErrorCode }
import formatter.FormatterFactory
import formatter.taozi.geo.DetailsEntryFormatter
import formatter.taozi.misc.CommentFormatter
import formatter.taozi.poi.DetailedPOIFormatter
import models.poi._
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Utils

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/7/25.
 */
object POICtrlScala extends Controller {
  def viewPOIInfo(poiDesc: String, @Key(tag = "poiId") spotId: String, @Key(tag = "cmtPage") commentPage: Int, @Key(tag = "cmtPageSize") commentPageSize: Int, @Key(tag = "rmdPage") rmdPage: Int, @Key(tag = "rmdPageSize") rmdPageSize: Int) = Action {
    request =>
      {
        //   获取图片宽度
        val imgWidthStr = request.getQueryString("imgWidth")
        val imgWidth = if (imgWidthStr nonEmpty) imgWidthStr.get.toInt else 0
        val commentClass = if ("restaurant".equals(poiDesc)) classOf[RestaurantComment] else classOf[Comment]

        val detailedPOIFormatter = FormatterFactory.getInstance(classOf[DetailedPOIFormatter], imgWidth)
        val poiInfo = poiDesc match { //ViewSpot
          case "vs" => {
            val viewSpot: ViewSpot = PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[ViewSpot], detailedPOIFormatter.getFilteredFields(classOf[ViewSpot]))
            viewSpot
          }
          //            case "hotel" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Hotel], detailedPOIFormatter.getFilteredFields(classOf[Hotel]))
          //            case "restaurant" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Restaurant], detailedPOIFormatter.getFilteredFields(classOf[Restaurant]))
          //            case "shopping" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Shopping], detailedPOIFormatter.getFilteredFields(classOf[Shopping]))
          //case _ => Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc)).toScala
        }

        if (poiInfo == null)
          throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId))
        val info = detailedPOIFormatter.formatNode(poiInfo)

        //val userIdStr = request.headers.get("UserId")
        //取得推荐
        val rmdEntities = PoiAPI.getPOIRmd(spotId, rmdPage, rmdPageSize)
        // 取得评论
        val commentsEntities: java.util.List[Comment] = MiscAPI.getComments(commentClass, new ObjectId(spotId), null, null, 0, commentPage, commentPageSize)

        val comformatter = FormatterFactory.getInstance(classOf[CommentFormatter])

        val comments = comformatter.formatNode(commentsEntities)

        //val userId = if (userIdStr nonEmpty) Some(userIdStr.get.toLong) else None

        val ret = new ObjectMapper().createObjectNode
        ret.put("comments", comments)
        if (poiDesc.equals("restaurant")) {
          ret.put("moreCommentsUrl", "http://api-dev.lvxingpai.com/poi/restaurants/" + spotId + "/commentsScala?poiType=" + poiDesc)
        } else if (poiDesc.equals("vs")) {
          // 获得同城的销售数据
          val lyMapping = PoiAPI.getTongChenPOI(poiInfo.getId())
          val lyMappingStr = if (lyMapping == null) "" else String.format("http://m.ly.com/scenery/scenerydetail_%s_0_0.html", lyMapping.getLyId())
          ret.put("lyPoiUrl", lyMappingStr)
        }
        ret
        //判断请求是何种格式
        val reqDataFormat = request.headers.get("Accept").get.toString
        if (reqDataFormat.contains("application/json")) {
          Utils.createResponse(ErrorCode.NORMAL, ret).toScala
        } else if (reqDataFormat.contains("text/html")) {
          Ok(views.html.desc(spotId, poiInfo))
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid http accept type: %s.", reqDataFormat)).toScala
        }
      }
  }

  def getTravelGuide(locId: String, field: String, poiDesc: String) = Action {
    request =>
      {

        val destKeyList = field match {
          case "tips" => seqAsJavaList(Seq(AbstractPOI.FD_TIPS))
          case "trafficInfo" => seqAsJavaList(Seq(AbstractPOI.FD_TRAFFICINFO))
          case "visitGuide" => seqAsJavaList(Seq(AbstractPOI.FD_VISITGUIDE))
        }

        val poiInfo: ViewSpot = poiDesc match {
          case "vs" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[ViewSpot], destKeyList)
          //        case "hotel" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Hotel], destKeyList)
          //        case "restaurant" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Restaurant], destKeyList)
          //        case "shopping" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Shopping], destKeyList)
          //        case _ => Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc)).toScala
        }
        // 获取图片宽度
        val imgWidthStr = request.getQueryString("imgWidth")
        val imgWidth = if (imgWidthStr nonEmpty) imgWidthStr.get.toInt else 0
        val result = new ObjectMapper().createArrayNode()
        field match {
          case "tips" => {
            val descNode = new ObjectMapper().createObjectNode()
            descNode.put("desc", "")
            result.add(descNode)
            val detailsEntryFormatter = FormatterFactory.getInstance(classOf[DetailsEntryFormatter], imgWidth)
            val contentsNode: JsonNode = if (poiInfo.getTips() != null) {
              new ObjectMapper().createObjectNode()
            } else {
              new ObjectMapper().valueToTree(detailsEntryFormatter.formatNode(poiInfo.getTips()))
            }
            result.add(contentsNode)
          }
          case "trafficInfo" => {
            val trafficInfo: JsonNode = new ObjectMapper().valueToTree(poiInfo.getTrafficInfo())
            result.add(trafficInfo)
          }
          case "visitGuide" => {
            val visitGuide: JsonNode = new ObjectMapper().valueToTree(poiInfo.getVisitGuide())
            result.add(visitGuide)
          }
        }
        Utils.createResponse(ErrorCode.NORMAL, result).toScala
      }
  }

}