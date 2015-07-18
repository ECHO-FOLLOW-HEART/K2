package controllers.bache

import java.util.Arrays

import akka.actor.FSM.->
import com.twitter.util.{ Future, FuturePool }
import exception.AizouException
import models.AizouBaseEntity
import models.geo.Country
import models.misc.{ Track, Album }
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

}
