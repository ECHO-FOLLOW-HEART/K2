package controllers.app

import api.GeoAPI
import com.fasterxml.jackson.databind.node.{ ObjectNode, ArrayNode }
import formatter.FormatterFactory
import formatter.taozi.geo.{ CountryExpertFormatter, SimpleCountryFormatter, SimpleLocalityFormatter, TerseLocalityFormatter }
import misc.TwitterConverter._
import models.AizouBaseEntity
import models.geo.{ Country, Locality }
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.Utils

import scala.collection.JavaConversions._

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

  /**
   * 获得国家列表
   * @param contCode
   * @return
   */
  def getCountriesByContinent(contCode: String) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleCountryFormatter])
      val fields = Seq(AizouBaseEntity.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME, Country.fnDesc, Country.fnCode, Country.fnImages, "rank")
      for {
        countryExperts <- if (contCode.equals("RCOM")) GeoAPI.getCountryRecommend(fields) else GeoAPI.getCountryByContCode(contCode, fields)
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    }
  )

  /**
   * 获得城市详情
   *
   * @param localityId
   * @return
   */
  def getLocality(localityId: String) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[TerseLocalityFormatter])
      val fields = Seq(Locality.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnImages, Locality.fnTravelMonth)
      for {
        locality <- GeoAPI.getLocalityById(new ObjectId(localityId), fields)
      } yield {
        val node = formatter.formatNode(locality).asInstanceOf[ObjectNode]
        node.put("commodityCnt", 0)
        Utils.status(node.toString).toScala
      }
    }
  )

  /**
   * 获得城市列表
   *
   * @return
   */
  def getLocalitiesByCountry(countryId: String) = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleLocalityFormatter])
      val fields = Seq(Locality.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnImages)
      for {
        localities <- GeoAPI.getLocalityByCountryCode(new ObjectId(countryId), fields)
      } yield {
        val node = formatter.formatNode(localities).asInstanceOf[ArrayNode]
        Utils.status(node.toString).toScala
      }
    }
  )

}
