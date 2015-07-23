package aizou.core

import com.twitter.util.{Future, FuturePool}
import exception.AizouException
import models.AizouBaseEntity
import models.guide.Guide
import models.misc.{Track, Album}
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query
import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/7/7.
 */
object UserUgcAPIScala {

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
      val tracks = JavaConversions.asScalaBuffer(query.asList())
      tracks.size match {
        case 0 => (0 -> 0)
        case _ => (tracks.size -> tracks.groupBy(_.getCountry.getId).size)
      }
    }
  }

}
