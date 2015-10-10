package controllers.app

import api.GeoAPI
import cache.GeoCache
import com.fasterxml.jackson.databind.node.{ ObjectNode, ArrayNode }
import formatter.FormatterFactory
import formatter.taozi.geo.{ LocalityFormatter, CountryExpertFormatter }
import org.bson.types.ObjectId
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
        countryExperts <- GeoAPI.getCountryExperts()
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    })

  def getLocalityById(id: String) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[LocalityFormatter], java.lang.Integer.valueOf(960))
      for {
        locality <- GeoCache.getLocality(new ObjectId(id))
      } yield {
        val node = formatter.formatNode(locality).asInstanceOf[ObjectNode]
        Utils.status(node.toString).toScala
      }
    })
}
