import _root_.cache.GeoCache
import com.google.inject.{ Guice, AbstractModule }
import play.api._

/**
 * Created by topy on 2015/10/8.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
