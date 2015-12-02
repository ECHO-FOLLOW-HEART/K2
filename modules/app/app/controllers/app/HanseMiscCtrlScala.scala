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

  /**
   * 添加旅客信息
   *
   * @param userId
   * @return
   */
  def createTravel(userId: Long) = redirectPost(s"http://192.168.100.3:9480/users/$userId/travellers")

  /**
   * 修改旅客信息
   *
   * @return
   */
  def updateTravel(key: String, userId: Long) = redirectPut(s"http://192.168.100.3:9480/users/$userId/travellers/$key")

  /**
   * 旅客信息列表
   *
   * @param userId
   * @return
   */
  def travelers(userId: Long) = redirects(s"http://192.168.100.3:9480/users/$userId/travellers")

  /**
   * 旅客信息
   *
   * @param userId
   * @param key
   * @return
   */
  def travelerByKey(key: String, userId: Long) = redirects(s"http://192.168.100.3:9480/users/$userId/travellers/$key")

  /**
   * 删除旅客信息
   *
   * @param userId
   * @param key
   * @return
   */
  def delTraveler(key: String, userId: Long) = redirectDelete(s"http://192.168.100.3:9480/users/$userId/travellers/$key")

  def redirectPost(url: String): Action[AnyContent] = Action.async(
    block = requestIn => {
    val requestOut: WSRequest = WS.url(url).withRequestTimeout(100000)
    val queryString = requestIn.queryString.map { case (k, v) => k -> v.mkString }
    val body = requestIn.body.asJson getOrElse Json.obj()
    val header = requestIn.headers.headers map (h => (h._1, h._2))
    // ("Content-Type" -> "application/json;charset=utf-8")
    val result = requestOut.withHeaders(header: _*).withQueryString(queryString.toList: _*).post(body)
    Response2Result(result)
  }
  )

  def redirectPut(url: String): Action[AnyContent] = Action.async(
    requestIn => {
      val requestOut: WSRequest = WS.url(url).withRequestTimeout(100000)
      val queryString = requestIn.queryString.map { case (k, v) => k -> v.mkString }
      val body = requestIn.body.asJson getOrElse Json.obj()
      val header = requestIn.headers.headers map (h => (h._1, h._2))
      // ("Content-Type" -> "application/json;charset=utf-8")
      val result = requestOut.withHeaders(header: _*).withQueryString(queryString.toList: _*).put(body)
      Response2Result(result)
    }
  )

  def redirectDelete(url: String): Action[AnyContent] = Action.async(
    requestIn => {
      val requestOut: WSRequest = WS.url(url).withRequestTimeout(100000)
      requestOut.get()
      val queryString = requestIn.queryString.map { case (k, v) => k -> v.mkString }
      val header = requestIn.headers.headers map (h => (h._1, h._2))
      // ("Content-Type" -> "application/json;charset=utf-8")
      Response2Result(requestOut.withHeaders(header: _*).withQueryString(queryString.toList: _*).delete())
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
