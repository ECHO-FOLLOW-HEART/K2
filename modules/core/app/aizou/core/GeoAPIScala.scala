package aizou.core

import com.twitter.util.{ Future, FuturePool }
import models.geo.CountryExpert
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/23.
 */
object GeoAPIScala {

  def getCountryExperts()(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[CountryExpert]] = {
    futurePool {
      val query = ds.createQuery(classOf[CountryExpert])
      query.asList()
    }
  }

}
