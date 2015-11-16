package controllers.app

import api.GeoAPI
import com.fasterxml.jackson.databind.node.ArrayNode
import formatter.FormatterFactory
import formatter.taozi.geo.{ SimpleCountryFormatter, CountryExpertFormatter }
import models.AizouBaseEntity
import models.geo.Country
import play.api.mvc.{ Action, Controller }
import scala.collection.JavaConversions._
import misc.TwitterConverter._
import utils.Implicits._
import utils.Utils

/**
 * Created by topy on 2015/7/23.
 */
object GeoCtrlScala extends Controller {

  /**
   * 查看有达人的国家
   *
   * @param withExperts
   * @return
   */
  def getCountryExperts(withExperts: Boolean) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[CountryExpertFormatter])
      for {
        countryExperts <- GeoAPI.getCountryExperts()
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    }
  )

  def getCountriesByContinent(contCode: String) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleCountryFormatter])
      val fields = Seq(AizouBaseEntity.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME, Country.fnDesc, Country.fnCode, Country.fnImages)
      for {
        countryExperts <- GeoAPI.getCountryByContCode(contCode, fields)
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    }
  )

}
