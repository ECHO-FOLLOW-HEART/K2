package controllers.app

import aizou.core.{ PoiAPI, MiscAPI }
import aspectj.Key
import com.fasterxml.jackson.databind.ObjectMapper
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
        // 获取图片宽度
        val imgWidthStr = request.getQueryString("imgWidth")
        val imgWidth: Integer = if (imgWidthStr nonEmpty) imgWidthStr.get.toInt else 0
        val commentClass = classOf[RestaurantComment]

        val detailedPOIFormatter: DetailedPOIFormatter[_ <: AbstractPOI] = FormatterFactory.getInstance(classOf[DetailedPOIFormatter[_ <: AbstractPOI]], imgWidth)
        val poiInfo = poiDesc match { //ViewSpot
          case "vs" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[ViewSpot], detailedPOIFormatter.getFilteredFields(classOf[ViewSpot]))
          case "hotel" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Hotel], detailedPOIFormatter.getFilteredFields(classOf[Hotel]))
          case "restaurant" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Restaurant], detailedPOIFormatter.getFilteredFields(classOf[Restaurant]))
          case "shopping" => PoiAPI.getPOIInfo(new ObjectId(spotId), classOf[Shopping], detailedPOIFormatter.getFilteredFields(classOf[Shopping]))
          case _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc))
        }
        if (poiInfo == null)
          throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId))
        // 判断请求是何种格式
        val reqDataFormat = request.headers.get("Accept").get.toString
        if (reqDataFormat.contains("application/json")) {
          val info = detailedPOIFormatter.formatNode(poiInfo)
          // 取得评论
          val commentsEntities: java.util.List[Comment] = MiscAPI.getComments(commentClass, new ObjectId(spotId), null, null, 0, commentPage, commentPageSize)
          val comformatter = FormatterFactory.getInstance(classOf[CommentFormatter])
          val comments = comformatter.formatNode(commentsEntities)

          val ret = new ObjectMapper().createObjectNode
          ret.set("comments", comments)
          if (poiDesc.equals("restaurant")) {
            val hostName = "api.lvxingpai.com"
            val url = routes.MiscCtrlScala.displayComment(poiDesc, spotId, 0, 1, 0, commentPage, commentPageSize, false).url
            ret.put("moreCommentsUrl", "http://" + hostName + url)
          } else if (poiDesc.equals("vs")) {
            // 获得同城的销售数据
            val lyMapping = PoiAPI.getTongChenPOI(poiInfo.getId)
            val lyMappingStr = if (lyMapping == null) "" else String.format("http://m.ly.com/scenery/scenerydetail_%s_0_0.html", lyMapping.getLyId())
            ret.put("lyPoiUrl", lyMappingStr)
          }
          Utils.createResponse(ErrorCode.NORMAL, ret).toScala
        } else if (reqDataFormat.contains("text/html")) {
          Ok(views.html.desc(poiInfo.asInstanceOf[ViewSpot]))
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

        val poiInfo = poiDesc match {
          case "vs" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[ViewSpot], destKeyList)
          case "hotel" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Hotel], destKeyList)
          case "restaurant" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Restaurant], destKeyList)
          case "shopping" => PoiAPI.getPOIInfo(new ObjectId(locId), classOf[Shopping], destKeyList)
          case _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc))
        }

        // 判断请求是何种格式
        val reqDataFormat = request.headers.get("Accept").get.toString
        if (reqDataFormat.contains("application/json")) {
          // 获取图片宽度
          val imgWidthStr = request.getQueryString("imgWidth")
          val imgWidth: Integer = if ((imgWidthStr nonEmpty) && (imgWidthStr != null)) imgWidthStr.get.toInt else 0
          val result = new ObjectMapper().createObjectNode()
          field match {
            case "tips" =>
              result.put("desc", "")
              val detailsEntryFormatter = FormatterFactory.getInstance(classOf[DetailsEntryFormatter], imgWidth)
              if (poiInfo.getTips != null) result.set("contents", new ObjectMapper().createObjectNode())
              else result.set("contents", detailsEntryFormatter.formatNode(poiInfo.getTips))
            case "trafficInfo" => result.set("contents", new ObjectMapper().valueToTree(poiInfo.getTrafficInfo))
            case "visitGuide" => result.set("contents", new ObjectMapper().valueToTree(poiInfo.getVisitGuide))
          }
          Utils.createResponse(ErrorCode.NORMAL, result).toScala
        } else if (reqDataFormat.contains("text/html")) {

          field match {
            case "trafficInfo" => Ok(views.html.traffic(poiInfo.getTrafficInfo))
            case "tips" => Ok(views.html.tips(poiInfo.getTips.toBuffer.toSeq))
            case "visitGuide" => Ok(views.html.experience(poiInfo.getVisitGuide))
            case _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", field))
          }
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid http accept type: %s.", reqDataFormat)).toScala
        }
      }
  }
}