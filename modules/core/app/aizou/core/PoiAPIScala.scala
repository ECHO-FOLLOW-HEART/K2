package aizou.core

import database.MorphiaFactory
import exception.{ ErrorCode, AizouException }
import models.AizouBaseEntity
import models.poi.{ Shopping, Restaurant, Hotel, ViewSpot }
import org.bson.types.ObjectId

/**
 * Created by pengyt on 2015/7/31.
 */
object PoiAPIScala {

  /**
   * 获得POI信息。
   */
  def getPOIInfo(poiId: ObjectId, poiDesc: String, fieldList: Seq[String]) = {
    val ds = MorphiaFactory.datastore
    val poiClass = poiDesc match {
      case "vs" => classOf[ViewSpot]
      case "hotel" => classOf[Hotel]
      case "restaurant" => classOf[Restaurant]
      case "shopping" => classOf[Shopping]
      case _ => throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc))
    }
    val query = ds.createQuery(poiClass).field("_id").equal(poiId).field(AizouBaseEntity.FD_TAOZIENA).equal(true)
    if (fieldList != null && fieldList.nonEmpty)
      query.retrievedFields(true, fieldList: _*)

    query.get
  }
}
