package controllers.app

import aizou.core.GeoAPIScala
import api.{ TravelNoteAPI, UserAPI }
import com.twitter.util.{ Future => TwitterFuture }
import scala.concurrent.{ Future => ScalaFuture }
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import formatter.FormatterFactory
import formatter.taozi.misc.{ SearchTravelNoteFormatterScala, TravelNoteFormatter }
import misc.TwitterConverter._
import models.user.ExpertInfo
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.Utils

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/9/11.
 */
object TravelNoteCtrlScala extends Controller {

  def getUsersTravelNotes(uid: Long) = Action.async(block = request => {
    val travelFormatter = FormatterFactory.getInstance(classOf[TravelNoteFormatter])
    travelFormatter.setLevel(TravelNoteFormatter.Level.SIMPLE)
    val future = for {
      expertInfo <- UserAPI.getExpertInfo(uid, Seq(ExpertInfo.fnTravelNote))
      travelNotes <- TravelNoteAPI.getTravelNote(expertInfo.getOrElse(new ExpertInfo()).getTravelNote)
    } yield {
      val node = travelFormatter.formatNode(travelNotes.getOrElse(Seq()))
      Utils.status(node.toString).toScala
    }
    future
  })

  def searchTravelNotes(query: String, page: Int, pageSize: Int) = Action.async(
    request => {
      for {
        travelNotes <- GeoAPIScala.searchTravelNote(query, page, pageSize)
      } yield {
        val mapper = new SearchTravelNoteFormatterScala().objectMapper
        val data = mapper.valueToTree[JsonNode](travelNotes)
        Utils.status(data.toString).toScala
      }
    })
}
