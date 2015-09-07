package api

import com.twitter.util.{ Future, FuturePool }
import exception.AizouException
import models.AizouBaseEntity
import models.geo.Locality
import models.guide.Guide
import models.misc.{ Album, Track }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query
import utils.TaoziDataFilter

import scala.collection.JavaConversions
import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/7.
 */
object UserUgcAPI {

  implicit def userInfoYunkai2Model(locality: Locality, userId: Long): Track = {
    val result = new Track()
    result.setId(new ObjectId)
    locality.setImages(TaoziDataFilter.getOneImage(locality.getImages))
    result.setLocality(locality)
    result.setCountry(locality.getCountry)
    result.setUserId(userId)
    result.setTaoziEna(true)
    result.setItemId
    return result
  }

  @throws(classOf[AizouException])
  def getGuidesCntByUser(uid: Long)(implicit ds: Datastore, futurePool: FuturePool): Future[Long] = {
    futurePool {
      val query: Query[Guide] = ds.createQuery(classOf[Guide])
      query.field(Guide.fnUserId).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
      query.countAll
    }
  }

  def getAlbumsCntByUser(uid: Long)(implicit ds: Datastore, futurePool: FuturePool): Future[Long] = {
    futurePool {
      val query: Query[Album] = ds.createQuery(classOf[Album])
      query.field(Album.FD_USERID).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
      query.countAll
    }
  }

  def getTrackCntAndCountryCntByUser(uid: Long)(implicit ds: Datastore, futurePool: FuturePool): Future[(Int, Int)] = {
    val query: Query[Track] = ds.createQuery(classOf[Track])
    query.field(Album.FD_USERID).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    futurePool {
      val tracks = query.asList()
      tracks.size match {
        case 0 => (0 -> 0)
        case _ => (tracks.size -> tracks.groupBy(_.getCountry.getId).size)
      }
    }
  }

  def fillTracks(userId: Long, ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Track]] = {
    val query: Query[Locality] = ds.createQuery(classOf[Locality])
    val clist = for { tempId <- ids } yield query.criteria("_id").equal(tempId)
    futurePool {
      val fieldList = Seq(AizouBaseEntity.FD_ID, Locality.fnLocation, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnImages, Locality.fnCountry)
      query.or(clist: _*)
      query.retrievedFields(true, fieldList: _*)
      query.asList().map(userInfoYunkai2Model(_, userId))
    }
  }

  object ActionCode extends Enumeration {

    case class ActionCode(value: String) extends Val(value)

    val LIKE = ActionCode("like")
    val UNLIKE = ActionCode("unlike")
  }

  object ItemTypeCode extends Enumeration {

    case class ItemTypeCode(value: String) extends Val(value)

    val LOCALITY = ItemTypeCode("locality")
  }
}
