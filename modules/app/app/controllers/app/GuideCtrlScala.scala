package controllers.app

import api.GuideAPI
import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.util.{ Future => TwitterFuture }
import database.MorphiaFactory
import misc.TwitterConverter._
import models.guide.Guide
import play.api.mvc.{ Action, Controller }
import utils.{ Result => K2Result }

import scala.language.postfixOps

/**
 * Created by zephyre on 7/20/15.
 */
object GuideCtrlScala extends Controller {

  implicit val futurePool = com.twitter.util.FuturePool.unboundedPool
  implicit val datastore = MorphiaFactory.datastore

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
      status <- (data \ "status").asOpt[String]
    } yield {
      val entries = Seq("status", "title") map (v => {
        val key = GuideAPI.GuideProps withName v
        val value = (data \ v).asOpt[String]

        key -> value
      }) filter (_._2 nonEmpty) map (v => v._1 -> v._2.get)

      val update = Map(entries: _*)

      // 转换
      (try {
        GuideAPI.updateGuideInfo(guideId, update) map (_ => K2Result.ok(None))
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
    val future = (for {
      data <- request.body.asJson
    } yield {
      val m: ObjectMapper = new ObjectMapper
      val guideUpdate: Guide = m.convertValue(data, classOf[Guide])
      (try {
        GuideAPI.updateGuide(guideId, guideUpdate, uid) map (_ => K2Result.ok(None))
      } catch {
        case _: NoSuchElementException => TwitterFuture(K2Result.unprocessable)
      }) rescue {
        case _: IllegalArgumentException => TwitterFuture(K2Result.unprocessable)
      }
    }) getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })
}
