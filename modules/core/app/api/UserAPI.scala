package api

import com.twitter.util.{ Future, FuturePool }
import models.user.{ ExpertRequest, UserProfile }
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/8/26.
 */
object UserAPI {

  def getUserInfo(targetId: Long, fields: Seq[String])(implicit ds: Datastore, futurePool: FuturePool): Future[Option[UserProfile]] = {
    val result = for {
      userMap <- getUsersByIdList(fields, targetId)
    } yield userMap.toSeq.head._2
    result
  }

  def getUsersByIdList(fields: Seq[String], targetIds: Long*)(implicit ds: Datastore, futurePool: FuturePool): Future[Map[Long, Option[UserProfile]]] = futurePool {
    import UserProfile._
    if (targetIds isEmpty) {
      Map[Long, Option[UserProfile]]()
    } else {
      val query = targetIds length match {
        case 1 => ds.createQuery(classOf[UserProfile]).field(fdUserId).equal(targetIds head)
        case _ => ds.createQuery(classOf[UserProfile]).field(fdUserId).in(seqAsJavaList(targetIds))
      }
      // 获得需要处理的字段名
      val allowedProperties = Seq(fdUserId, fdProfile, fdTags)
      val retrievedFields = (fields filter (allowedProperties.contains(_))) ++ Seq(fdUserId)

      query.retrievedFields(true, retrievedFields: _*)

      val results = Map(query.asList() map (item => item.userId -> item): _*)

      Map(targetIds map (v => v -> (results get v)): _*)
    }
  }

  def findExpertRequest(userId: Long, tel: String)(implicit ds: Datastore, futurePool: FuturePool): Future[Long] = futurePool {
    ds.createQuery(classOf[ExpertRequest]).field(ExpertRequest.fnUserId).equal(userId).countAll()
  }

  def expertRequest(userId: Long, tel: String)(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = futurePool {
    val expertRe = new ExpertRequest()
    expertRe.setUserId(userId)
    expertRe.setTel(tel)
    ds.save[ExpertRequest](expertRe)
  }

  //  def updateUserInfo(targetId: Long, fields: Seq[String])(implicit ds: Datastore, futurePool: FuturePool): Future[UserProfile] = futurePool{
  //    val query = ds.createQuery(classOf[UserProfile]).field(UserProfile.fdUserId).equal(targetId)
  //      .retrievedFields(true, fields: _*)
  //    val updateOps = fields.foldLeft(ds.createUpdateOperations(classOf[UserProfile]))((ops, entry) => {
  //      val (key, value) = entry
  //      ops.set(key, value)
  //    })
  //
  //    val result = ds.findAndModify(query, updateOps)
  //
  //    if (result == null)
  //      throw new AizouException(s"Cannot find user: $targetId")
  //    else
  //      result
  //  }

}
