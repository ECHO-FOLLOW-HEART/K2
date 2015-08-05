package controllers.app

import aizou.core.MiscAPI
import exception.{ AizouException, ErrorCode }
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
  // 协议
  def eula() = Action {
    Ok(views.html.eula())
  }

  //公司介绍
  def about(version: String) = Action {
    Ok(views.html.about(version))
  }
}
