package api

import com.twitter.util.FuturePool
import database.MorphiaFactory
import models.AizouBaseEntity
import models.geo.Locality
import models.user.UgcInfo
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query

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
}
