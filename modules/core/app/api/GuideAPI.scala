package api

import com.twitter.util.{ Future => TwitterFuture, FuturePool }
import models.guide.{ AbstractGuide, Guide }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.language.postfixOps

/**
 * Created by zephyre on 7/20/15.
 */
object GuideAPI {
  object GuideStatus extends Enumeration {
    val planned = Value("planned")
    val traveled = Value("traveled")
  }

  object GuideProps extends Enumeration {
    val Status = Value("status")
    val Title = Value("title")
  }

  /**
   * 修改行程单
   * @return
   */
  def updateGuideInfo(guideId: String, updateInfo: Map[GuideProps.Value, String])(implicit ds: Datastore, futurePool: FuturePool) = {
    if (updateInfo isEmpty)
      TwitterFuture(())
    else {
      futurePool {
        val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId)
        val ops = updateInfo.foldLeft(ds.createUpdateOperations(classOf[Guide]))((ops, entry) => {
          entry._1 match {
            case item if item.id == GuideProps.Status.id => ops.set(Guide.fnStatus, GuideStatus withName entry._2 toString)
            case item if item.id == GuideProps.Title.id => ops.set(AbstractGuide.fnTitle, entry._2.trim)
          }
        })

        ds.updateFirst(query, ops)
        ()
      }
    }
  }

  /**
   * 更新用户行程单的状态
   * @return
   */
  def updateGuideStatus(guideId: String, status: GuideStatus.Value)(implicit ds: Datastore, futurePool: FuturePool) = {
    futurePool {
      val query = ds.createQuery(classOf[Guide]) field "id" equal new ObjectId(guideId)
      val ops = ds.createUpdateOperations(classOf[Guide]).set(Guide.fnStatus, status.toString)
      ds.updateFirst(query, ops)
    }
  }
}
