package controllers.bache

import java.util.Arrays

import com.twitter.util.{Future, FuturePool}
import exception.AizouException
import models.AizouBaseEntity
import models.geo.Country
import models.misc.Album
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/15.
 */
object BatchImpl {

  def getTrackByCountrys(ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Long] = {
    val query: Query[Album] = ds.createQuery(classOf[Album])
    query.field(Album.FD_USERID).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    futurePool {
      query.countAll
    }
  }

  @throws(classOf[AizouException])
  def getCountriesByNames(keywords: Seq[String], page: Int, pageSize: Int)(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Country]] = {
    val query: Query[Country] = ds.createQuery(classOf[Country])
    query.field(Country.fnAlias).hasAnyOf(keywords)
    query.field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    query.retrievedFields(true, Arrays.asList(Country.FN_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME, Country.fnImages): _*)
    query.offset(page * pageSize).limit(pageSize)
    futurePool {
      query.asList().toSeq
    }
  }

}
