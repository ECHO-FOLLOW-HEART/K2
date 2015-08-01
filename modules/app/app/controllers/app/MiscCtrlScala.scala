package controllers.app

import aizou.core.MiscAPI
import exception.{AizouException, ErrorCode}
import formatter.FormatterFactory
import formatter.taozi.misc.CommentFormatter
import models.poi.RestaurantComment
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Utils

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 7/24/15.
 */
object MiscCtrlScala extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def comments(id: String) = Action {
    val results = MiscAPI.getComments[RestaurantComment](classOf[RestaurantComment], new ObjectId(id), 0.0, 1.0, 0, 0, 15)
    Ok(views.html.comments(id, 1, 20, "restaurant", results))
  }
  def displayComment(poiType: String, poiId: String, lower: Double, upper: Double, lastUpdate: Long, page: Int, pageSize: Int, isHtmlComplete: Boolean) = Action {
    request =>
      {
        val formatter = poiType match {
          case "restaurant" => FormatterFactory.getInstance(classOf[CommentFormatter])
          case  _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType))
        }

        // 判断请求是何种格式
        val reqDataFormat = request.headers.get("Accept").get.toString
        if (reqDataFormat.contains("application/json")) {
          val commentList = MiscAPI.getComments[RestaurantComment](classOf[RestaurantComment], new ObjectId(poiId), lower, upper, lastUpdate, page, pageSize)
          val results = formatter.formatNode((Option(commentList) map (_.toSeq)).get)
          Utils.createResponse(ErrorCode.NORMAL, results).toScala
        } else if (reqDataFormat.contains("text/html")) {
          if (isHtmlComplete) {
            val results = MiscAPI.getComments[RestaurantComment](classOf[RestaurantComment], new ObjectId(poiId), lower, upper, lastUpdate, page, pageSize)
            Ok(views.html.comments(poiId, page, pageSize, poiId, results))
          } else {
            val results = MiscAPI.getComments[RestaurantComment](classOf[RestaurantComment], new ObjectId(poiId), lower, upper, lastUpdate, page, pageSize)
            Ok(views.html.commentsEntry(poiId, page, pageSize, poiId, results))
          }
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid http accept type: %s.", reqDataFormat)).toScala
        }
      }
  }

  // 协议
  def agreement() = Action {
    Ok(views.html.agreement())
  }
  //公司介绍
  def about(version: String) = Action {
    Ok(views.html.about(version))
  }
}
