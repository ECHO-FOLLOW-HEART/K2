import javax.inject.{Inject, Singleton}

import com.google.inject.{AbstractModule, Guice}
import play.api.cache._

/**
 * Created by topy on 2015/10/15.
 */
abstract class CacheService(cache: CacheApi) {

}

@Singleton
class CacheServiceImpl @Inject()(@NamedCache("k2-cache") k2Cache: CacheApi) extends CacheService(k2Cache) {
  var cache = k2Cache
}

object CacheHandler {

  def getCacheInstance[A](controllerClass: Class[A]): A = {
    val injector = Guice.createInjector(new AbstractModule {
      protected def configure() {
        bind(classOf[CacheService]).to(classOf[CacheServiceImpl])
      }
    })
    injector.getInstance(controllerClass)
  }
}
