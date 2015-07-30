package aizou.core

import java.util

import com.twitter.util.{ Future, FuturePool }
import models.geo.CountryExpert
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.util.Random

/**
 * Created by topy on 2015/7/23.
 */
object GeoAPIScala {

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

}
