package controllers.app

import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import play.api.mvc.{Action, AnyContent, ResponseHeader, Result}
import utils.{Result => K2Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/11/21.
 */
object HanseMiscCtrlScala {

  /**
   * 首页专栏
   * @return
   */
  def columns() = redirects("http://192.168.100.3:9480/columns")

  /**
   * 首页推荐
   *
   * @return
   */
  def recommend() = redirects("http://192.168.100.3:9480/recommend")

  /**
   * 商品列表
   *
   * @param topicType
   * @return
   */
  def commodities(topicType: String) = {
    if (topicType != null || topicType.equals(""))
      redirects("http://192.168.100.3:9480/marketplace/commodities")
    else
      redirects("http://192.168.100.3:9480/columns/commodities")
  }

  /**
   * 商品详情
   *
   * @param id
   * @return
   */
  def commodityById(id: String) = redirects("http://192.168.100.3:9480/marketplace/commodities/" + id)

  /**
   * 商品分类列表
   *
   * @return
   */
  def commoditiesCategories() = redirects("http://192.168.100.3:9480/marketplace/commodities/categories")

  /**
   * 商户详情
   *
   * @param id
   * @return
   */
  def sellerById(id: String) = redirects("http://192.168.100.3:9480/marketplace/sellers/" + id)

  /**
   * 订单详情
   *
   * @param id
   * @return
   */
  def orderById(id: String) = redirects("http://192.168.100.3:9480/marketplace/orders/" + id)

  /**
   * 订单状态
   *
   * @param id
   * @return
   */
  def ordersStatusById(id: String) = redirects("http://192.168.100.3:9480/marketplace/orders/" + id + "/status")

  /**
   * 订单列表
   *
   * @return
   */
  def orders() = redirects("http://192.168.100.3:9480/marketplace/orders")

  /**
   * 创建订单
   *
   * @return
   */
  def createOrder() = redirectPost("http://192.168.100.3:9480/marketplace/orders")

  def redirectPost(url: String): Action[AnyContent] = Action.async(
    requestIn => {
      val requestOut: WSRequest = WS.url(url).withRequestTimeout(100000)
      val queryString = requestIn.queryString.map { case (k, v) => k -> v.mkString }
      val body = requestIn.body.asJson getOrElse Json.obj()
      val result = requestOut.withHeaders("Content-Type" -> "application/json").withQueryString(queryString.toList: _*).post(body)
      Response2Result(result)
    }
  )

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
