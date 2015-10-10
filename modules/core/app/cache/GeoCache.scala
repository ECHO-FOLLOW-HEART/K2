package cache

import com.twitter.util.{ Future, FuturePool }
import database.MorphiaFactory
import models.geo.Locality
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import play.api.Play.current
import play.api.cache.Cache

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/10/9.
 */
object GeoCache {

  val LOCALITY_KEY = "Locality"
  val LOCALITY_NAME_TO_ID = "Locality_NAME_ID"

  def saveLocalities(): Unit = {
    val ds: Datastore = MorphiaFactory.datastore
    val globalLocality: Seq[Locality] = ds.createQuery(classOf[Locality]).asList()
    val locZhNameMap = globalLocality.map(t => t.getZhName -> t.getId)(collection.breakOut): Map[String, ObjectId]
    Cache.set(LOCALITY_KEY, globalLocality.groupBy(_.getId))
    // Cache.set(LOCALITY_NAME_TO_ID, locZhNameMap)
  }

  def getLocality(id: ObjectId)(implicit futurePool: FuturePool): Future[Locality] = {
    futurePool {
      val localityMap = Cache.getAs[Map[ObjectId, Seq[Locality]]](LOCALITY_KEY)
      val localities = localityMap.getOrElse(Map.empty).get(id)
      localities.getOrElse(Seq()).get(0)
    }

  }

}
