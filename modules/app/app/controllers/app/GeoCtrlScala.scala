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
        stats <- GeoAPI.getGeoStats(countryExperts map (_.getId))
      } yield {
        val node = formatter.formatNode(countryExperts).asInstanceOf[ArrayNode]
        node foreach (c => {
          val stat = stats.get(new ObjectId(c.get("id").asText()))
          c.asInstanceOf[ObjectNode].put("commoditiesCnt", stat map (_.commodityCount) getOrElse 0)
        })
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
      val fields = Seq(Locality.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnImages, Locality.fnTravelMonth, Locality.fnDesc)
      val id = new ObjectId(localityId)
      for {
        locality <- GeoAPI.getLocalityById(id, fields)
        stats <- GeoAPI.getGeoStats(Seq(id))
      } yield {
        val node = formatter.formatNode(locality).asInstanceOf[ObjectNode]
        node.put("playGuide", "http://h5.taozilvxing.com/city/items.php?tid=" + locality.getId.toString)
        node.put("trafficInfoUrl", "http://h5.taozilvxing.com/city/traff-list.php?tid=" + locality.getId.toString)
        val stat = stats get id
        node.put("commoditiesCnt", stat map (_.commodityCount) getOrElse 0)
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
        stats <- GeoAPI.getGeoStats(localities map (_.getId))
      } yield {
        val node = formatter.formatNode(localities).asInstanceOf[ArrayNode]
        node foreach (c => {
          val stat = stats.get(new ObjectId(c.get("id").asText()))
          c.asInstanceOf[ObjectNode].put("commoditiesCnt", stat map (_.commodityCount) getOrElse 0)
        })
        Utils.status(node.toString).toScala
      }
    }
  )

}
