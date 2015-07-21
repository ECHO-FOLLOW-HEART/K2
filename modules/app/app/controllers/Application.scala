package controllers

import aizou.core.MiscAPI
import models.poi.RestaurantComment
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }

import scala.collection.JavaConversions._

/**
 * Created by zephyre on 7/21/15.
 */
object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def comments(id: String) = Action {
    val results = MiscAPI.getComments[RestaurantComment](classOf[RestaurantComment], new ObjectId(id), 0.0, 1.0, 0, 0, 15)
    Ok(views.html.comments(id, 1, results))
  }

}
