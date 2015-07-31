package controllers.app

import aizou.core.{ PoiAPI, GeoAPIScala }
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.fasterxml.jackson.databind.node.ArrayNode
import exception.{ AizouException, ErrorCode }
import formatter.FormatterFactory
import formatter.taozi.geo.{ DetailsEntryFormatter, CountryExpertFormatter }
import models.geo.{ DetailsEntry, Locality }
import play.api.mvc.{ Action, Controller }
import scala.collection.JavaConversions._
import misc.TwitterConverter._
import utils.Implicits._
import utils.Utils

import org.bson.types.ObjectId

/**
 * Created by topy on 2015/7/23.
 */
object GeoCtrlScala extends Controller {

  def getCountryExperts(withExperts: Boolean) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[CountryExpertFormatter])
      for {
        countryExperts <- GeoAPIScala.getCountryExperts()
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    })

  def getTravelGuide(locId: String, field: Option[String]) = Action {
    request =>
      {
        if (field nonEmpty) {
          // 获取图片宽度
          val imgWidthStr = request.getQueryString("imgWidth")
          val imgWidth: Integer = if (imgWidthStr nonEmpty) imgWidthStr.get.toInt else 0
          val fieldList: java.util.List[String] = field.get match {
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
            case _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT")
          }

          val locality = PoiAPI.getLocalityByField(new ObjectId(locId), fieldList)
          if (locality == null) Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality is not exist.ID:" + locId).toScala

          // 判断请求是何种格式
          val reqDataFormat = request.headers.get("Accept").get.toString
          if (reqDataFormat.contains("application/json")) {
            val result = new ObjectMapper().createObjectNode()
            val detailsEntryFormatter = FormatterFactory.getInstance(classOf[DetailsEntryFormatter], imgWidth)
            field.get match {
              case "remoteTraffic" => {
                result.put("desc", "")
                result.set("contents", detailsEntryFormatter.formatNode(locality.getRemoteTraffic()))
              }
              case "localTraffic" => {
                result.put("desc", "")
                result.set("contents", detailsEntryFormatter.formatNode(locality.getLocalTraffic()))
              }
              case "activities" => {
                result.put("desc", locality.getActivityIntro())
                result.set("contents", detailsEntryFormatter.formatNode(locality.getActivities()))
              }
              case "tips" => {
                result.put("desc", "")
                result.set("contents", detailsEntryFormatter.formatNode(locality.getTips()))
              }
              case "geoHistory" => {
                result.put("desc", "")
                result.set("contents", detailsEntryFormatter.formatNode(locality.getGeoHistory()))
              }
              case "specials" => {
                result.put("desc", "")
                result.set("contents", detailsEntryFormatter.formatNode(locality.getSpecials()))
              }
              case "desc" => {
                result.put("desc", locality.getDesc())
                result.set("contents", new ObjectMapper().createObjectNode())
              }
              case "dining" => {
                result.put("desc", locality.getDiningIntro())
                result.set("contents", detailsEntryFormatter.formatNode(locality.getCuisines()))
              }
              case "shopping" => {
                result.put("desc", locality.getShoppingIntro())
                result.set("contents", detailsEntryFormatter.formatNode(locality.getCommodities()))
              }
              case "diningTitles" => {
                result.put("contentsTitles", contentsTitles(locality.getCuisines().toBuffer.toSeq))
              }
              case "shoppingTitles" => {
                result.put("contentsTitles", contentsTitles(locality.getCommodities().toBuffer.toSeq))
              }
            }
            Utils.createResponse(ErrorCode.NORMAL, result).toScala
          } else if (reqDataFormat.contains("text/html")) {
            field.get match {
              case "dining" => {
                Ok(views.html.dining(locality.getDiningIntro, locality.getCuisines.toBuffer.toSeq))
              }
            }
          } else {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid http accept type: %s.", reqDataFormat)).toScala
          }
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT").toScala
        }
      }
  }

  def contentsTitles(entries: Seq[DetailsEntry]): String = {
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
