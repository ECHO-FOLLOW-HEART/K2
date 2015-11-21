package controllers.app

import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, Action, ResponseHeader, Result}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/21.
 */
object HanseMiscCtrlScala {

  def columns() = redirects("http://192.168.100.3:9480/columns")

  def recommend() = redirects("http://192.168.100.3:9480/recommend")

  def commoditiesByType(topicType: String) = redirects("http://192.168.100.3:9480/columns/commodities")

  def commoditiesById(id: String) = redirects("http://192.168.100.3:9480/commodities/" + id)

  def redirects(url: String): Action[AnyContent] = Action.async(
    requestIn => {
      val requestOut: WSRequest = WS.url(url).withRequestTimeout(100000)
      requestOut.get()
      val queryString = requestIn.queryString.map { case (k, v) => k -> v.mkString }
      Response2Result(requestOut.withQueryString(queryString.toList: _*).get())
    }
  )

  def Response2Result(response: Future[WSResponse]): Future[Result] = {
    response map {
      response =>
        Result(ResponseHeader(response.status, response.allHeaders map {
          h => (h._1, h._2.head)
        }), Enumerator(response.bodyAsBytes))
    }
  }

}
