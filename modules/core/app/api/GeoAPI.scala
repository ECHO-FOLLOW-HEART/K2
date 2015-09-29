package api

import java.util

import com.twitter.util.{ Future, FuturePool }
import models.geo.{ Country, CountryExpert, Locality }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.util.Random
import scala.language.postfixOps

/**
 * Created by topy on 2015/7/23.
 */
object GeoAPI {

  def getCountryExperts()(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[CountryExpert]] = {
    futurePool {
      val query = ds.createQuery(classOf[CountryExpert])
      query.asList() map (c => {
        val images = c.getImages
        if (images != null && images.size() > 1) {
          val idx = Random.nextInt(images.size())
          c.setImages(util.Arrays.asList(images.get(idx)))
        }
        c
      })
    }
  }

  // TODO 处理Fields的问题
  def getCountryByNames(names: Seq[String], fields: Seq[String] = Seq())(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Country]] = {
    futurePool {
      if (names == null || names.isEmpty)
        Seq()
      else {
        val query = ds.createQuery(classOf[Country]).field(Country.FD_ZH_NAME).in(names) //.retrievedFields(true, fields: _*)
        (if (fields nonEmpty) query.retrievedFields(true, fields: _*) else query).asList()
      }
    }
  }

  def getCountryByIds(ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Country]] = {
    futurePool {
      if (ids == null || ids.isEmpty)
        Seq()
      else {
        val query = ds.createQuery(classOf[Country]).field(Country.FN_ID).in(ids).retrievedFields(true, Seq(Country.FD_ZH_NAME): _*)
        query.asList()
      }
    }
  }

  // TODO 处理fields
  def getLocalityByNames(names: Seq[String], fields: Seq[String] = Seq())(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Locality]] = {
    futurePool {
      if (names == null || names.isEmpty)
        Seq()
      else {
        val query = ds.createQuery(classOf[Locality]).field(Locality.FD_ZH_NAME).in(names) //.retrievedFields(true, fields: _*)
        (if (fields nonEmpty) query.retrievedFields(true, fields: _*) else query).asList()
      }
    }
  }

  def getLocalityByIds(ids: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[Locality]] = {
    futurePool {
      if (ids == null || ids.isEmpty)
        Seq()
      else {
        val query = ds.createQuery(classOf[Locality]).field(Country.FN_ID).in(ids).retrievedFields(true, Seq(Country.FN_ID, Locality.FD_ZH_NAME): _*)
        query.asList()
      }
    }
  }

}
