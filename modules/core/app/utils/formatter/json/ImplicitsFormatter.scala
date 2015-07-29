package utils.formatter.json

import models.geo.{ Locality => K2Locality }
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Entity
import play.api.libs.json.Json

/**
 * Created by topy on 2015/7/29.
 */
object ImplicitsFormatter {

  @Entity(noClassnameStored = true)
  case class Locality(id: String, zhName: String, enName: String)

  implicit def locality2Model(locality: Locality): K2Locality = {
    val l: K2Locality = new K2Locality()
    l.setId(new ObjectId(locality.id))
    l.setZhName(locality.zhName)
    l.setEnName(locality.enName)
    l
  }

  implicit val localityReads = Json.reads[Locality]

}
