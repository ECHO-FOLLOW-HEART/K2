package api

import com.twitter.util.{ Future, FuturePool }
import models.misc.TravelNote
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/9/11.
 */
object TravelNoteAPI {

  def getTravelNote(itemIds: Seq[ObjectId])(implicit ds: Datastore, futurePool: FuturePool): Future[Option[Seq[TravelNote]]] = futurePool {
    if (itemIds.size == 0)
      None
    else {
      val query = ds.createQuery(classOf[TravelNote])
      query.field(TravelNote.fnId).in(itemIds)
      query.retrievedFields(true, Seq(TravelNote.fnId, TravelNote.fnTitle, TravelNote.fnImages, TravelNote.fnSummary): _*)
      Some(query.asList())
    }
  }

}
