package controllers.app

import java.{ util, lang }

import com.fasterxml.jackson.databind.node.ArrayNode
import com.lvxingpai.yunkai.UserInfo
import com.twitter.util.Future
import controllers.bache.{ BatchImpl, BatchUtils }
import formatter.FormatterFactory
import formatter.taozi.geo.SimpleCountryFormatter
import misc.FinagleConvert
import misc.TwitterConverter._
import models.geo.{ Locality, Country }
import models.misc.Track
import models.user.{ UserInfo => K2UserInfo }
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.Utils

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/14.
 */
object Batch extends Controller {

  def locality2Track(locality: Locality, userId: Long): Track = {
    val track = new Track()
    track.setId(new ObjectId)
    track.setUserId(userId)
    track.setCountry(locality.getCountry)

    val tempLoc = new Locality()
    tempLoc.setZhName(locality.getZhName)
    tempLoc.setEnName(locality.getEnName)
    tempLoc.setImages(util.Arrays.asList(locality.getImages.get(0)))
    tempLoc.setLocation(locality.getLocation)
    track.setLocality(tempLoc)
    //track.setItemId()
    track.setEnabled(true)
    track
  }

  /**
   * 根据国家生成足迹的配置文件
   *
   * @return
   */
  def createExpertTrackByCountry() = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleCountryFormatter])
      for {
        countries <- BatchImpl.getCountriesByNames(Seq("日本", "韩国", "中国"), 0, 999)
      } yield {
        val node = formatter.formatNode(countries).asInstanceOf[ArrayNode]
        dealWithCountries(countries)
        Utils.status(node.toString).toScala
      }
    })

  def dealWithCountries(countries: Seq[Country]): Unit = {
    val userCnt = BatchImpl.getCountryToUserCntMap(countries.map(_.getId))
    writeCountries(countries, userCnt)
  }

  def writeCountries(countries: Seq[Country], userCnt: Map[ObjectId, Int]): Unit = {
    val subKey = "country"
    val keyMid = "."
    val keys = (subKey + Country.FD_ZH_NAME, subKey + Country.FD_EN_NAME, subKey + Country.fnImages, subKey + "expertCnt")

    def writeOneCountry(country: Country, userCnt: Map[ObjectId, Int]): Seq[String] = {
      val imageUrl = country.getImages match {
        case null => "null"
        case _ if country.getImages.get(0) == null => "null"
        case _ if country.getImages.get(0) != null => String.format("http://images.taozilvxing.com/%s?imageView2/2/w/ 640", country.getImages.get(0).getKey)
      }
      Seq(
        BatchUtils.writeLine(keys._1 + keyMid + country.getId.toString, country.getZhName),
        BatchUtils.writeLine(keys._2 + keyMid + country.getId.toString, country.getEnName),
        BatchUtils.writeLine(keys._3 + keyMid + country.getId.toString, imageUrl),
        BatchUtils.writeLine(keys._4 + keyMid + country.getId.toString, userCnt.get(country.getId).getOrElse(0).toString)
      )
    }
    val contents = countries.flatMap(writeOneCountry(_, userCnt))
    BatchUtils.makeConfFile(contents)
  }

  def userInfoToTrack() = Action.async(
    request => {
      for {
        users <- BatchImpl.getTracksFromUserInfo()
        map <- usersToMap(users)
      } yield {
        map.foreach(
          kv => {
            val localityFuture = for (userId <- kv._1; localities <- kv._2) yield localities.map(locality2Track(_, userId))
            for {
              locality <- localityFuture
              re <- BatchImpl.saveTracks(locality)
            } yield Utils.status("").toScala
          }
        )
        Utils.status("").toScala
      }

    }
  )

  def usersToMap(users: Seq[K2UserInfo]): Future[Map[Future[lang.Long], Future[Seq[Locality]]]] = {
    Future {
      Map(users map { user =>
        Future {
          user.getUserId
        } -> BatchImpl.getLocalitiesByIds(user.getTracks.map(_.getId))
      }: _*)
    }
  }
}
