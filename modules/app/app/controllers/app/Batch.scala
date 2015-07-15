package controllers.app

import com.fasterxml.jackson.databind.node.{ ArrayNode, ObjectNode }
import controllers.bache.BatchImpl
import formatter.FormatterFactory
import formatter.taozi.geo.SimpleCountryFormatter
import misc.TwitterConverter._
import play.api.mvc.{ Action, Controller }
import utils.Utils
import utils.Implicits._

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/14.
 */
object Batch extends Controller {

  def createExpertTrackByCountry() = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleCountryFormatter])
      for {
        countries <- BatchImpl.getCountriesByNames(Seq("日本"), 0, 999)
      } yield {
        val node = formatter.formatNode(countries).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    })

}
