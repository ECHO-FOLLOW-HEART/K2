package controllers.app

import aizou.core.GeoAPIScala
import com.fasterxml.jackson.databind.node.ArrayNode
import formatter.FormatterFactory
import formatter.taozi.geo.CountryExpertFormatter
import play.api.mvc.{ Action, Controller }
import scala.collection.JavaConversions._
import misc.TwitterConverter._
import utils.Implicits._
import utils.Utils

/**
 * Created by topy on 2015/7/23.
 */
object GeoCtrlScala extends Controller {

  def getCountryExperts(withExperts: Boolean) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[CountryExpertFormatter])
      for {
        countryExperts <- GeoAPIScala.getCountryExperts()
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    })
}
