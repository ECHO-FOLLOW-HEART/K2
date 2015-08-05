package controllers.app

import aizou.core.{ GeoAPI, PoiAPI, GeoAPIScala }
import com.fasterxml.jackson.databind.{ ObjectMapper }
import com.fasterxml.jackson.databind.node.ArrayNode
import exception.{ AizouException, ErrorCode }
import formatter.FormatterFactory
import formatter.taozi.geo.{ DetailsEntryFormatter, CountryExpertFormatter }
import models.geo.{ DetailsEntry, Locality }
import play.api.mvc.{ Action, Controller }
import scala.collection.JavaConversions._
import misc.TwitterConverter._
import utils.Implicits._
import utils.Utils

import org.bson.types.ObjectId

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
