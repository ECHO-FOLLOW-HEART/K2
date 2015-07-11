package aizou.core

import database.MorphiaFactory
import exception.AizouException
import models.AizouBaseEntity
import models.guide.Guide
import models.misc.Album
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.Query
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/7/7.
 */
object UserUgcAPIScala {

  @throws(classOf[AizouException])
  def getGuidesCntByUser(uid: Long): Future[Long] = {
    Future {
      val ds: Datastore = MorphiaFactory.datastore
      val query: Query[Guide] = ds.createQuery(classOf[Guide])
      query.field(Guide.fnUserId).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
      query.countAll
    }
  }

  def getAlbumsCntByUser(uid: Long): Future[Long] = {
    Future {
      val ds: Datastore = MorphiaFactory.datastore
      val query: Query[Album] = ds.createQuery(classOf[Album])
      query.field(Album.FD_USERID).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
      query.countAll
    }
  }

}
