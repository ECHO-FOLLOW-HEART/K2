package controllers.app

import java.util

import api.{ MiscAPI, UserUgcAPI, GuideAPI }
import api.GuideAPI.GuideProps
import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.util.{ Future => TwitterFuture }
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.guide.GuideTemplateFormatter
import formatter.taozi.misc.HotSearchFormatter
import misc.TwitterConverter._
import models.guide.Guide
import org.bson.types.ObjectId
import play.api.libs.json.Json
import play.api.mvc.{ AnyContent, Action, Controller }
import utils.{ Result => K2Result, Utils }
import utils.Implicits._
import utils.formatter.json.ImplicitsFormatter._

import scala.language.postfixOps
import scala.collection.JavaConversions._

/**
 * Created by zephyre on 7/20/15.
 */
object GuideCtrlScala extends Controller {

  /**
   * 更新行程单的状态
   *
   * @param uid
   * @param guideId
   * @return
   */
  def updateGuideInfo(uid: Long, guideId: String) = Action.async(request => {
    val future = (for {
      data <- request.body.asJson
    } yield {
      val entries = Seq("status", "title", "localities") map (v => {
        val key = GuideAPI.GuideProps withName v

        val dataValue = (data \ v)

        val value: Option[Any] = key match {
          case item if item.id == GuideProps.Status.id => dataValue.asOpt[String]
          case item if item.id == GuideProps.Title.id => dataValue.asOpt[String]
          case item if item.id == GuideProps.Localities.id => dataValue.asOpt[Array[Locality]]
        }
        key -> value
      }) filter (_._2 nonEmpty) map (v => v._1 -> v._2.get)
      val update = Map(entries: _*)
      // 转换
      (try {
        for {
          updateInfo <- GuideAPI.updateGuideInfo(guideId, update)
          guide <- GuideAPI.updateTracks(uid, guideId, update)
        } yield K2Result.ok(None)
      } catch {
        case _: NoSuchElementException => TwitterFuture(K2Result.unprocessable)
      }) rescue {
        case _: IllegalArgumentException => TwitterFuture(K2Result.unprocessable)
      }
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  /**
   * 保存攻略或更新攻略
   *
   * @return
   */
  def saveGuideInfo(uid: Long, guideId: String) = Action.async(request => {
    val data = request.body.asJson
    val m: ObjectMapper = new ObjectMapper
    val guideUpdate: Guide = m.convertValue(m.readTree(data.toString), classOf[Guide])
    (try {
      GuideAPI.updateGuide(guideId, guideUpdate, uid) map (_ => K2Result.ok(None))
    } catch {
      case _: NoSuchElementException => TwitterFuture(K2Result.unprocessable)
    }) rescue {
      case _: IllegalArgumentException => TwitterFuture(K2Result.unprocessable)
    }
  })

  def getTempGuide(locId: String): Action[AnyContent] = Action.async {
    request =>
      {
        val guideFormatter = FormatterFactory.getInstance(classOf[GuideTemplateFormatter], java.lang.Integer.valueOf(200))
        val future = for {
          guideTemp <- GuideAPI.getTempGuide(new ObjectId(locId))
        } yield {
          if (guideTemp isEmpty)
            K2Result.notFound(ErrorCode.DATA_NOT_FOUND, "Guide not exists.")
          else {
            val node = guideFormatter.formatNode(util.Arrays.asList(guideTemp.get))
            Utils.status(node.toString).toScala
          }
        }
        future
      }
  }
}
