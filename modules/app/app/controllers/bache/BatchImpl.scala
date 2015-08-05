package controllers.bache

import java.util.Arrays

import akka.actor.FSM.->
import com.twitter.util.{ Future, FuturePool }
import exception.AizouException
import models.AizouBaseEntity
import models.geo.{ CountryExpert, Locality, Country }
import models.misc.{ Track, Album }
import models.poi.{ AbstractPOI, ViewSpot }
import models.user.UserInfo
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.{ UpdateOperations, Query }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/15.
 */
object BatchImpl {

  def getCountryToUserCntMap(ids: Seq[ObjectId], uids: Seq[Long])(implicit ds: Datastore, futurePool: FuturePool): Map[ObjectId, Int] = {
    val query = ds.createQuery(classOf[Track])
    query.field(Track.fnCountry + ".id").in(ids).field(Track.fnUserId).in(uids)
    val tracks = query.asList()
    val ctMap = tracks.groupBy(_.getCountry.getId)
    for ((k, v) <- ctMap) yield (k, v.groupBy(_.getUserId).size)
  }

  @throws(classOf[AizouException])
  def getCountriesByNames(keywords: Seq[String], page: Int, pageSize: Int)(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Country]] = {
    val query = ds.createQuery(classOf[Country])
    query.field(Country.fnAlias).hasAnyOf(keywords)
    //query.field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    query.retrievedFields(true, Arrays.asList(Country.FN_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME, Country.fnImages, Country.fnCode, "zhCont", "enCont", "contCode", "rank"): _*)
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

  def getViewSportLocalList()(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[ViewSpot]] = {
    val query = ds.createQuery(classOf[ViewSpot])
    query.retrievedFields(true, Arrays.asList(AizouBaseEntity.FD_ID, AbstractPOI.simplocList): _*)
    futurePool {
      query.asList().toSeq
    }
  }

  //  def saveViewSportLocality(map: Map[ObjectId, Locality])(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
  //    val update: UpdateOperations[ViewSpot] = ds.createUpdateOperations(classOf[ViewSpot])
  //    val query: Query[ViewSpot] = ds.createQuery(classOf[ViewSpot]).field(AizouBaseEntity.FD_ID).in(map.keySet)
  //    update.set(AbstractPOI.FD_LOCALITY, tuple._2)
  //    futurePool {
  //      ds.update(query, update)
  //    }
  //  }

  def saveTracks(tracks: Seq[Track])(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
    futurePool {
      ds.save(tracks)
    }
  }

  def saveCountryExpert(cExperts: Seq[CountryExpert])(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
    futurePool {
      ds.save(cExperts)
    }
  }

}
