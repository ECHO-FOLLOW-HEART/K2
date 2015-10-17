package controllers.app

import javax.inject.Inject

import cache.GeoCache
import com.fasterxml.jackson.databind.node.ObjectNode
import com.twitter.util.Future
import formatter.FormatterFactory
import formatter.taozi.geo.LocalityFormatter
import misc.TwitterConverter
import misc.TwitterConverter._
import org.bson.types.ObjectId
import play.api.cache.{ CacheApi, NamedCache }
import play.api.mvc.{ Action, Controller }
import utils.Implicits._
import utils.Utils

/**
 * Created by topy on 2015/7/23.
 */
class CacheCtrl @Inject() (@NamedCache("k2-cache") cache: CacheApi) extends Controller {

  def getLocality() = Action.async(
    request => {
      Future {
        cache.set("name1", "1")
        Utils.status(cache.get("name33").get).toScala
      }
    })
}
