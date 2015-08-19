package api

import com.twitter.util.{ Future => TwitterFuture, FuturePool }

import database.MorphiaFactory
import models.AizouBaseEntity
import models.guide.{ GuideTemplate, AbstractGuide, Guide, ItinerItem }
import models.geo.{ Locality => K2Locality }
import models.misc.Track
import utils.Implicits._
import models.poi._
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.UpdateOperations
import scala.collection.JavaConversions._
import utils.formatter.json.ImplicitsFormatter._

import scala.language.postfixOps

/**
 * Created by zephyre on 7/20/15.
 */
object GuideAPI {

  implicit def guide2UgcGuide(guide: GuideTemplate, userId: Long): Guide = {
    val result = new Guide()
    result.setUserId(userId.toInt)
    result.setId(new ObjectId)
    result.localities = guide.localities
    result.setUpdateTime(System.currentTimeMillis)
    result.itinerary = guide.itinerary
    result.shopping = guide.shopping
    result.restaurant = guide.restaurant
    result.setItineraryDays(0)
    result.images = guide.getImages
    //设置攻略可见性
    result.setVisibility(Guide.fnVisibilityPublic)
    //设置攻略状态
    result.setStatus(Guide.fnStatusPlanned)
    // 保存攻略时，置为可用
    result.setTaoziEna(true)
    result
  }

  object GuideStatus extends Enumeration {
    val planned = Value("planned")
    val traveled = Value("traveled")
  }

  object GuideProps extends Enumeration {
    val Status = Value("status")
    val Title = Value("title")
    val Localities = Value("localities")
  }

  def updateTracks(uid: Long, guideId: String, updateInfo: Map[GuideProps.Value, Any]) = {
    if (updateInfo.get(GuideProps.Status) isEmpty)
      TwitterFuture()
    else {
      for {
        guide <- GuideAPI.getGuide(guideId)
        tracks <- UserUgcAPI.fillTracks(uid, guide.localities.map(_.getId))
        updateFp <- GuideAPI.updateFootprints(uid, updateInfo, tracks)
      } yield TwitterFuture()
    }
  }

  def getGuide(guideId: String)(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId)
      query.get()
    }
  }

  //  def addGuideToUser(uid: Long) = {
  //    for {
  //      guide <- GuideAPI.getGuideTemplate()
  //      result <- GuideAPI.addGuideTemplate(uid, guide)
  //    } yield TwitterFuture()
  //  }
  //
  //  def getGuideTemplate()(implicit ds: Datastore, futurePool: FuturePool) = {
  //    futurePool {
  //      val fieldList = Seq(AizouBaseEntity.FD_ID, K2Locality.fnLocation, K2Locality.FD_ZH_NAME, K2Locality.FD_EN_NAME, K2Locality.fnImages, K2Locality.fnCountry)
  //      val query = ds.createQuery(classOf[GuideTemplate]).field("locId").equal(new ObjectId("5473ccd7b8ce043a64108c46"))
  //        .retrievedFields(true, fieldList: _*)
  //      query.get()
  //    }
  //  }

  //  def addGuideTemplate(uid: Long, guide: GuideTemplate)(implicit ds: Datastore, futurePool: FuturePool) = {
  //    futurePool {
  //      ds.save[Guide](guide2UgcGuide(guide, uid))
  //    }
  //  }

  /**
   * 修改行程单
   * @return
   */
  def updateGuideInfo(guideId: String, updateInfo: Map[GuideProps.Value, Any])(implicit ds: Datastore, futurePool: FuturePool) = {
    if (updateInfo isEmpty)
      TwitterFuture(())
    else {
      futurePool {
        val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId)
        val ops = updateInfo.foldLeft(ds.createUpdateOperations(classOf[Guide]))((ops, entry) => {
          entry._1 match {
            case item if item.id == GuideProps.Status.id => ops.set(Guide.fnStatus, GuideStatus withName entry._2.asInstanceOf[String] toString)
            case item if item.id == GuideProps.Title.id => ops.set(AbstractGuide.fnTitle, entry._2.asInstanceOf[String] trim)
            case item if item.id == GuideProps.Localities.id => ops.set(AbstractGuide.fnLocalities, localities2Model(entry._2.asInstanceOf[Array[Locality]]))
          }
        })
        ds.updateFirst(query, ops)
      }
    }
  }

  def updateFootprints(userId: Long, updateInfo: Map[GuideProps.Value, Any], tracks: Seq[Track])(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      if (updateInfo.get(GuideProps.Status) isEmpty)
        TwitterFuture()
      else {
        val status = updateInfo.get(GuideProps.Status)
        if (status.get.equals(GuideStatus.traveled.toString)) {
          ds.findAndDelete[Track](ds.createQuery(classOf[Track]).field(Track.fnItemId).in(tracks.map(_.getItemId)))
          ds.save[Track](tracks)
        } else {
          val query = ds.createQuery(classOf[Track]).field(Track.fnUserId).equal(userId).field(Track.fnLocalityId).in(tracks map (_.getLocality.getId))
          val ops = ds.createUpdateOperations(classOf[Track]).set(AizouBaseEntity.FD_TAOZIENA, false)
          ds.update(query, ops)
        }
      }
    }
  }

  /**
   * 更新用户行程单的状态
   * @return
   */
  def updateGuideStatus(guideId: String, status: GuideStatus.Value)(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId)
      val ops = ds.createUpdateOperations(classOf[Guide]).set(Guide.fnStatus, status.toString)
      ds.updateFirst(query, ops)
    }
  }

  /**
   * 更新行程单
   *
   * @param guideId
   * @param guide
   * @throws exception.AizouException
   */
  def updateGuide(guideId: String, guide: Guide, userId: Long)(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId) field "userId" equal userId
      if (query.iterator.hasNext) {
        val update: UpdateOperations[Guide] = ds.createUpdateOperations(classOf[Guide])
        if (guide.itinerary != null) {
          fillPOIType(guide.itinerary)
          update.set(AbstractGuide.fnItinerary, guide.itinerary)
        }
        if (guide.getItineraryDays != null) update.set(Guide.fnItineraryDays, guide.getItineraryDays)
        if (guide.shopping != null) update.set(AbstractGuide.fnShopping, guide.shopping)
        if (guide.restaurant != null) update.set(AbstractGuide.fnRestaurant, guide.restaurant)
        if (guide.images != null) update.set(AbstractGuide.fnImages, guide.images)
        if (guide.localities != null) update.set(AbstractGuide.fnLocalities, guide.localities)
        if (guide.getVisibility != null) update.set(Guide.fnVisibility, guide.getVisibility)
        if (guide.getStatus != null) update.set(Guide.fnStatus, guide.getStatus)
        if (guide.getUpdateTime != null) update.set(Guide.fnUpdateTime, System.currentTimeMillis)
        ds.update(query, update)
      }
    }
  }

  def fillPOIType(itinerary: Seq[ItinerItem]): Unit = {
    for (item <- itinerary) {
      val poi = item.poi
      poi match {
        case poi: ViewSpot => item.poi.`type` = "vs"
        case poi: Restaurant => item.poi.`type` = "restaurant"
        case poi: Shopping => item.poi.`type` = "shopping"
        case poi: Hotel => item.poi.`type` = "hotel"
      }
    }
  }

}
