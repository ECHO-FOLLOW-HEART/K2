package utils.formatter.json

import models.geo.{ Locality => K2Locality }
import models.user.{ Contact => K2Contact }
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Entity
import play.api.libs.json.Json
import scala.collection.JavaConverters._

/**
 * Created by topy on 2015/7/29.
 */
object ImplicitsFormatter {

  @Entity(noClassnameStored = true)
  case class Locality(id: String, zhName: String, enName: String)

  case class JsonContact(entryId: Int, sourceId: Int, name: String, tel: String)

  implicit val localityReads = Json.reads[Locality]
  implicit val contactReads = Json.reads[JsonContact]

  implicit def contact2Model(contact: JsonContact): K2Contact = {
    val l: K2Contact = new K2Contact()
    l.setId(new ObjectId())
    l.setEntryId(contact.entryId)
    l.setSourceId(contact.sourceId)
    l.setName(contact.name)
    l.setTel(contact.tel)
    l
  }

  implicit def contact2Model(contacts: Seq[JsonContact]): java.util.List[K2Contact] = {
    contacts.map(contact2Model(_)).asJava
  }

  implicit def locality2Model(locality: Locality): K2Locality = {
    val l: K2Locality = new K2Locality()
    l.setId(new ObjectId(locality.id))
    l.setZhName(locality.zhName)
    l.setEnName(locality.enName)
    l
  }

  implicit def localities2Model(localities: Seq[Locality]): java.util.List[K2Locality] = {
    localities.map(locality2Model(_)).asJava
  }

}
