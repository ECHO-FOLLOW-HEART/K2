package controllers.app

import api.{ MiscAPI, UserUgcAPI }
import com.twitter.util.{ Future => TwitterFuture }
import misc.TwitterConverter._
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.{ Result => K2Result }

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

  def vote(itemType: String, itemId: String, uid: Long) = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
    } yield {
      val userId = if (uid > 0) uid else (body \ "userId").asOpt[Long].get
      MiscAPI.vote(userId, MiscAPI.ActionCode.ADD.value, itemType, new ObjectId(itemId)) map (_ => K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  def noVote(itemType: String, itemId: String, uid: Long) = Action.async(request => {
    MiscAPI.vote(uid, MiscAPI.ActionCode.DEL.value, itemType, new ObjectId(itemId)) map (_ => K2Result.ok(None))
  })
}
