package controllers.bache

import java.lang
import java.util.Arrays

import akka.actor.FSM.->
import com.twitter.util.{ Future, FuturePool }
import exception.AizouException
import models.AizouBaseEntity
import models.geo.{ Locality, Country }
import models.misc.{ Track, Album }
import models.user.UserInfo
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/15.
 */
object BatchImpl {

  def getCountryToUserCntMap(ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Map[ObjectId, Int] = {
    val query = ds.createQuery(classOf[Track])
    query.field(Track.fnCountry + ".id").in(ids)
    val tracks = query.asList()
    val ctMap = tracks.groupBy(_.getCountry.getId)
    for ((k, v) <- ctMap) yield (k, v.groupBy(_.getUserId).size)
  }

  @throws(classOf[AizouException])
  def getCountriesByNames(keywords: Seq[String], page: Int, pageSize: Int)(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Country]] = {
    val query = ds.createQuery(classOf[Country])
    query.field(Country.fnAlias).hasAnyOf(keywords)
    //query.field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    query.retrievedFields(true, Arrays.asList(Country.FN_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME, Country.fnImages): _*)
    query.offset(page * pageSize).limit(pageSize)
    futurePool {
      query.asList().toSeq
    }
  }

  def getTracksFromUserInfo()(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[UserInfo]] = {
    val query = ds.createQuery(classOf[UserInfo])
    query.field(UserInfo.fnTracks).exists()
    query.retrievedFields(true, Arrays.asList(UserInfo.fnUserId, UserInfo.fnTracks): _*)
    futurePool {
      query.asList().toSeq
    }
  }

  def getLocalitiesByIds(ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Locality]] = {
    val query = ds.createQuery(classOf[Locality])
    query.field(AizouBaseEntity.FD_ID).in(ids)
    //query.field(AizouBaseEntity.FD_TAOZIENA)
    query.retrievedFields(true, Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME,
      Locality.fnCountry, Locality.fnLocation, Locality.fnImages): _*)
    futurePool {
      query.asList().toSeq
    }
  }

  def saveTracks(tracks: Seq[Track])(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
    futurePool {
      ds.save(tracks)
    }
  }

}
