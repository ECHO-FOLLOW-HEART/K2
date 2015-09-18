package api

import com.twitter.util.{ Future, FuturePool }
import models.AizouBaseEntity
import models.geo.Locality
import models.misc.HotSearch
import models.poi.AbstractPOI
import models.user.ExpertRequest
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/7/7.
 */
object MiscAPI {

  object ItemTypeCodes extends Enumeration {

    case class ItemTypeCode(value: String) extends Val(value)

    val LOCALITY = ItemTypeCode("localities")
  }

  object ActionCode extends Enumeration {

    case class ActionCode(value: String) extends Val(value)

    val ADD = ActionCode("add")
    val DEL = ActionCode("del")
  }

  def vote(userId: Long, actions: String, itemType: String, itemId: ObjectId)(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      val query = itemType match {
        case ItemTypeCodes.LOCALITY.value => ds.createQuery(classOf[Locality]).field(AizouBaseEntity.FD_ID).equal(itemId)
        case _ => null
      }
      val update = actions match {
        case ActionCode.ADD.value => ds.createUpdateOperations(classOf[Locality]).add(Locality.fnVotes, userId, false)
        case ActionCode.DEL.value => ds.createUpdateOperations(classOf[Locality]).removeAll(Locality.fnVotes, userId)
        case _ => null
      }
      ds.findAndModify(query, update)
    }
  }

  def getHotResearch(itemType: String)(implicit ds: Datastore, futurePool: FuturePool): Future[Seq[HotSearch]] = {
    futurePool {
      val query = ds.createQuery(classOf[HotSearch])
        .field(HotSearch.fnIitemType).equal(itemType)
        .retrievedFields(true, Seq(HotSearch.fnItemId, HotSearch.fnZhName, AizouBaseEntity.FD_ID): _*)
      query.asList()
    }
  }
}
