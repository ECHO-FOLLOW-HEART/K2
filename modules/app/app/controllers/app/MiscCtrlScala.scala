package controllers.app

import api._
import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.util.{ Future => TwitterFuture }
import formatter.FormatterFactory
import formatter.taozi.geo.SearchLocalityFormatter
import formatter.taozi.misc.{ ReferenceFormatter, HotSearchFormatter }
import misc.TwitterConverter._
import models.AizouBaseEntity
import models.geo.{ Locality, Country }
import models.misc.Reference
import models.poi.AbstractPOI
import org.bson.types.ObjectId
import play.api.mvc.{ AnyContent, Action, Controller }
import utils.Implicits._
import utils.{ Result => K2Result, Utils }

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

  /**
   * 达人申请
   *
   * @return
   */
  def expertRequest() = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
      userId <- (body \ "userId").asOpt[Long] orElse Some(0L)
    } yield {
      UserAPI.expertRequest(userId, tel) map (_ => K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  /**
   * 热门搜索
   *
   * @param itemType
   * @return
   */
  def getHotSearch(itemType: String): Action[AnyContent] = Action.async {
    request =>
      {
        val hotFormatter = FormatterFactory.getInstance(classOf[HotSearchFormatter])
        val future = for {
          hot <- MiscAPI.getHotResearch(itemType)
        } yield {
          val node = hotFormatter.formatNode(hot)
          Utils.status(node.toString).toScala
        }
        future
      }
  }

  def search(query: String, scope: String): Action[AnyContent] = Action.async {
    request =>
      {
        val future = for {
          qLoc <- GeoAPI.getLocalityByNames(Seq("北京", "上海"), Seq(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME,
            Locality.fnImages, Locality.fnDesc))
        } yield {
          val node = FormatterFactory.getInstance(classOf[SearchLocalityFormatter]).formatNode(qLoc)
          Utils.status(node.toString).toScala
        }
        future
      }
  }

  /**
   * 搜索的辅助信息
   * @param query
   * @param scope
   * @return
   */
  def searchAncInfo(query: String, scope: String): Action[AnyContent] = Action.async {
    request =>
      {
        val future = for {
          qLoc <- GeoAPI.getLocalityByNames(Seq(query), Seq(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME,
            Locality.fnDinningIntro, Locality.fnShoppingIntro))
        } yield {
          val node = (new ObjectMapper()).createObjectNode()
          if (qLoc nonEmpty) {
            //node.put("itemType", "locality")
            val loc = qLoc.get(0)
            //node.put("itemId", loc.getId.toString)
            scope match {
              case "shopping" =>
                node.put("desc", GuideCtrl.removeH5Label(loc.getShoppingIntro))
                node.put("detailUrl", "http://h5.taozilvxing.com/city/shopping.php?tid=" + loc.getId.toString)
              case "restaurant" =>
                node.put("desc", GuideCtrl.removeH5Label(loc.getDiningIntro))
                node.put("detailUrl", "http://h5.taozilvxing.com/city/dining.php?tid=" + loc.getId.toString)
            }
          }
          Utils.status(node.toString).toScala
        }
        future
      }
  }

  /**
   * 取得推荐信息
   *
   * @param itemType
   * @param isAbroad
   * @return
   */
  def getRmd(itemType: String, isAbroad: Boolean): Action[AnyContent] = Action.async {
    request =>
      {
        val hotFormatter = FormatterFactory.getInstance(classOf[ReferenceFormatter])
        val future = for {
          hot <- MiscAPI.getReference(itemType, isAbroad)
        } yield {
          val node = hotFormatter.formatNode(hot)
          Utils.status(node.toString).toScala
        }
        future
      }
  }

}
