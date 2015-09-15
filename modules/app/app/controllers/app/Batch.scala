package controllers.app

import java.{ util, lang }

import com.fasterxml.jackson.databind.node.ArrayNode
import com.lvxingpai.yunkai.UserInfo
import com.twitter.util.Future
import controllers.bache.{ BatchImpl, BatchUtils }
import database.MorphiaFactory
import formatter.FormatterFactory
import formatter.taozi.geo.SimpleCountryFormatter
import formatter.taozi.user.TrackFormatter
import misc.FinagleConvert
import misc.TwitterConverter._
import models.geo._
import models.misc.Track
import models.user.{ UserInfo => K2UserInfo, ExpertInfo }
import org.bson.types.ObjectId
import play.Configuration
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.{ TaoziDataFilter, Utils }

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.Option

/**
 * Created by topy on 2015/7/14.
 */
object Batch extends Controller {

  val experts: Seq[Long] = Seq(
    11000, 100000, 100003, 100057,
    100076, 100093, 100001, 100015,
    100025, 100002, 100004, 100005,
    100009, 100010, 100011, 100012,
    100014, 100031, 100035, 100040,
    100056, 100067, 100068, 100073,
    100089, 100090, 100091, 201098,
    201099, 201101, 201097, 201033,
    201117, 201118, 201119, 201120,
    201121, 201122, 201123, 201124,
    201125, 201126, 201206, 201353,
    201354, 201392, 201394, 201385,
    201383, 201398, 201401
  )

  def locality2Track(locality: Locality, userId: Long): Track = {
    val track = new Track()
    track.setId(new ObjectId)
    track.setUserId(userId)
    track.setCountry(locality.getCountry)

    val tempLoc = new Locality()
    tempLoc.setId(locality.getId)
    tempLoc.setZhName(locality.getZhName)
    tempLoc.setEnName(locality.getEnName)
    tempLoc.setImages(util.Arrays.asList(locality.getImages.get(0)))
    tempLoc.setLocation(locality.getLocation)
    track.setLocality(tempLoc)
    track.setItemId()
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
        countries <- BatchImpl.getCountriesByNames(Seq("日本", "韩国", "中国", "泰国", "马来西亚", "新加坡", "印度尼西亚", "越南",
          "斯里兰卡", "阿联酋", "尼泊尔", "柬埔寨", "法国", "希腊", "意大利", "瑞士", "美国", "英国", "西班牙",
          "智利", "阿根廷", "巴西", "新西兰", "澳大利亚", "加拿大", "墨西哥", "马尔代夫", "丹麦", "芬兰"
        //"毛里求斯", "塞舌尔", "肯尼亚", "南非", "埃及", "卢森堡", "瑞典",
        //"葡萄牙", "比利时", "奥地利", "挪威", "土耳其", "荷兰", "俄罗斯", "德国"
        ), 0, 999)
      } yield {
        val node = formatter.formatNode(countries).asInstanceOf[ArrayNode]
        dealWithCountries(countries)
        Utils.status(node.toString).toScala
      }
    })

  def dealWithCountries(countries: Seq[Country]): Unit = {
    //    val config: Configuration = Configuration.root
    //    val map = config.getObject("experts")
    //    val list = map.asInstanceOf[java.util.HashMap].values
    val userCnt = BatchImpl.getCountryToUserCntMap(countries.map(_.getId), Seq(
      11000, 100000, 100003, 100057,
      100076, 100093, 100001, 100015,
      100025, 100002, 100004, 100005,
      100009, 100010, 100011, 100012,
      100014, 100031, 100035, 100040,
      100056, 100067, 100068, 100073,
      100089, 100090, 100091, 201098,
      201099, 201101, 201097, 201033,
      201117, 201118, 201119, 201120,
      201121, 201122, 201123, 201124,
      201125, 201126, 201206, 201353,
      201354, 201392, 201394, 201385,
      201383, 201398, 201401
    ))
    saveCountries(countries, userCnt)
    //writeCountries(countries, userCnt)
  }

  def saveCountries(countries: Seq[Country], userCnt: Map[ObjectId, Int]): Unit = {

    def country2CountryExpert(country: Country, userCnt: Map[ObjectId, Int]): CountryExpert = {
      val countryExpert = new CountryExpert()
      countryExpert.setId(country.getId)
      countryExpert.setZhName(country.getZhName)
      countryExpert.setEnName(country.getEnName)
      countryExpert.setImages(country.getImages)
      countryExpert.setCode(country.getCode)
      countryExpert.setRank(country.getRank)
      countryExpert.setImages(TaoziDataFilter.getOneImage(country.getImages))
      countryExpert.setExpertCnt(scala.Int.box(userCnt.get(country.getId).getOrElse(0)))

      val continent = new Continent()
      continent.setId(new ObjectId)
      continent.setZhName(country.getZhCont)
      continent.setEnName(country.getEnCont)
      continent.setCode(country.getContCode)
      countryExpert.setContinent(continent)

      countryExpert
    }
    val contents = countries map (country2CountryExpert(_, userCnt))
    BatchImpl.saveCountryExpert(contents)
    //BatchUtils.makeConfFile(contents)
  }

  /**
   * 把国家达人信息写入配置文件
   *
   * @param countries
   * @param userCnt
   */
  def writeCountries(countries: Seq[Country], userCnt: Map[ObjectId, Int]): Unit = {
    val subKey = "country"
    val keyMid = "."
    val keys = (subKey + Country.FD_ZH_NAME, subKey + Country.FD_EN_NAME, subKey + Country.fnImages, subKey + "expertCnt")

    def writeOneCountry(country: Country, userCnt: Map[ObjectId, Int]): Seq[String] = {
      val imageUrl = country.getImages match {
        case null => "null"
        case _ if country.getImages.size() == 0 => "null"
        case _ if country.getImages.size() > 0 => String.format("\"http://images.taozilvxing.com/%s?imageView2/2/w/640\"", country.getImages.get(0).getKey)
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
        mapToSave(map)
        Utils.status("Success").toScala
      }
    }
  )

  def mapToSave(map: Map[Future[lang.Long], Future[Seq[Locality]]]) = {
    map.foreach(
      kv => {
        val localityFuture = for (userId <- kv._1; localities <- kv._2) yield localities.map(locality2Track(_, userId))
        for {
          locality <- localityFuture
          re <- BatchImpl.saveTracks(locality)
        } yield re
      }
    )
  }

  def usersToMap(users: Seq[K2UserInfo]): Future[Map[Future[lang.Long], Future[Seq[Locality]]]] = {
    Future {
      Map(users map { user =>
        Future {
          user.getUserId
        } -> BatchImpl.getLocalitiesByIds(user.getTracks.map(_.getId))
      }: _*)
    }
  }

  def checkVsLocality(abroad: Boolean) = Action.async(
    request => {
      for {
        viewSpots <- BatchImpl.getViewSportLocalList(abroad)
      } yield {
        viewSpots map (BatchImpl.saveViewSportLocalityChina(_))
        Utils.status("Success" + viewSpots.size).toScala
      }
    }
  )

  def createExpertInfo(tracks: Seq[Track]): Seq[ExpertInfo] = {
    tracks.groupBy(_.getUserId).toSeq.map(x => {
      val expert = new ExpertInfo()
      expert.setId(new ObjectId())
      expert.setUserId(x._1)
      expert.setProfile("旅行派达人")
      expert.setZone(x._2.map(_.getLocality.getId))
      expert
    })
  }

  def refreshExpertInfo() = Action.async(
    request => {
      val jsonNode = request.body.asJson.get
      val userIds = (jsonNode \ "userIds").asOpt[Array[Long]]
      val userIdsValue = userIds.getOrElse(Array.emptyLongArray).toSeq
      for {
        tracks <- BatchImpl.getTracks(userIdsValue)
        result <- BatchImpl.saveExpertInfo(createExpertInfo(tracks))
      } yield {
        null
      }
    }
  )

  def updateExpertInfo() = Action.async(
    request => {
      val ds = MorphiaFactory.datastore
      val experts = ds.createQuery(classOf[ExpertInfo]).asList()

      for (expert <- experts) {
        val query = ds.createQuery(classOf[ExpertInfo]).field("id").equal(expert.getUserId)
        val zoneList = expert.getZone
        var newZoneList = zoneList.toList.toSeq
        for (zone <- zoneList) {
          val locality = ds.createQuery(classOf[Locality]).field("id").equal(zone).get
          if (locality != null) {
            val cid = locality.getCountry.getId
            newZoneList = newZoneList ++ Seq(cid)
          }
        }
        val result = seqAsJavaList(newZoneList.toSet.toSeq)
        val updateOps = ds.createUpdateOperations(classOf[ExpertInfo]).set(ExpertInfo.fnZone, result)
        ds.update(query, updateOps)
      }
      Future { Utils.status("success").toScala }
    })
}
