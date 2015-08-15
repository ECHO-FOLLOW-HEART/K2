package controllers.bache

import java.util.Arrays

import com.twitter.util.{ Future, FuturePool }
import exception.AizouException
import models.AizouBaseEntity
import models.geo.{ CountryExpert, Locality, Country }
import models.misc.Track
import models.poi.{ AbstractPOI, ViewSpot }
import models.user.UserInfo
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

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

  val capIdList = Arrays.asList(new ObjectId("5473ccd7b8ce043a64108c46"), new ObjectId("546f2daab8ce0440eddb2aff"),
    new ObjectId("5473ccd7b8ce043a64108c4d"), new ObjectId("5473ccd6b8ce043a64108c08"))

  def getViewSportLocalList(abroad: Boolean)(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[ViewSpot]] = {
    val query = ds.createQuery(classOf[ViewSpot])
    query.retrievedFields(true, Arrays.asList(AizouBaseEntity.FD_ID, AbstractPOI.simplocList, AbstractPOI.detTargets): _*)
    if (!abroad) {
      query.field("country.zhName").equal("中国").field(AbstractPOI.simplocList).notEqual(null)
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("5473ccd7b8ce043a64108c46"))
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("546f2daab8ce0440eddb2aff"))
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("5473ccd7b8ce043a64108c4d"))
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("5473ccd6b8ce043a64108c08"))
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("5473ccd7b8ce043a64108c45"))
      query.field(AbstractPOI.detTargets).notEqual(new ObjectId("5473ccd6b8ce043a64108c09"))
    } else {
      query.field(AbstractPOI.simplocList).notEqual(null).field("zhName").in(Seq("格兰维尔岛", "好运岛和帕斯巨石公园"))
      query.or(
        query.criteria("country").notEqual(null).criteria("country.id").notEqual(new ObjectId("5434d70e10114e684bb1b4ee")),
        query.criteria(AbstractPOI.simplocList).notEqual(null).criteria("locList.id").notEqual(new ObjectId("5434d70e10114e684bb1b4ee")))
      //query.field("country").notEqual(null).field("country.zhName").notEqual("中国").field(AbstractPOI.simplocList).notEqual(null)
    }

    futurePool {
      query.asList().toSeq
    }
  }

  def saveViewSportLocalityChina(vs: ViewSpot)(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
    if (vs.locList == null)
      Future()
    else {
      val query = ds.createQuery(classOf[ViewSpot]).field(AizouBaseEntity.FD_ID).equal(vs.getId)
      // val index = if (vs.locList.size() > 2) vs.locList.get(2) else vs.locList.get(vs.locList.size() - 1)
      val index = vs.locList.get(vs.locList.size() - 1)
      val update = ds.createUpdateOperations(classOf[ViewSpot]).set(AbstractPOI.FD_LOCALITY, index)

      futurePool {
        ds.update(query, update)
      }
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
