package controllers.app

import api.UserUgcAPI
import com.lvxingpai.yunkai.UserInfoProp
import com.lvxingpai.yunkai.UserInfoProp._
import com.twitter.util.{ Future => TwitterFuture }
import misc.TwitterConverter._
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.{ Result => K2Result }

/**
 * Created by topy on 2015/8/17.
 */
object UgcCtrlScala extends Controller {

  def updateUgcInfo(uid: Long) = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
    } yield {
      val action = (body \ "action").asOpt[String].getOrElse("")
      val itemId = (body \ "itemId").asOpt[String].getOrElse("")
      val itemType = (body \ "itemType").asOpt[String].getOrElse("")
      UserUgcAPI.updateUgcInfo(uid, action, itemType, new ObjectId(itemId)) map (_ => K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })
}
