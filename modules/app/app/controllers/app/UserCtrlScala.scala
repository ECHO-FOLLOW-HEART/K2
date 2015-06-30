package controllers.app

import aizou.core.UserAPI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.yunkai.{NotFoundException, UserExistsException, UserInfo => YunkaiUserInfo, UserInfoProp}
import com.twitter.util.{Future => TwitterFuture}
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.user.{UserFormatterOld, UserInfoFormatter}
import misc.TwitterConverter._
import misc.{FinagleConvert, FinagleFactory}
import models.user.UserInfo
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller, Result}
import utils.phone.PhoneParserFactory
import utils.{MsgConstants, Utils}

import scala.concurrent.Future
import scala.language.{implicitConversions, postfixOps}

/**
 * Created by zephyre on 6/30/15.
 */
object UserCtrlScala extends Controller {

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  val basicUserInfoFieds = Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar, UserInfoProp.Gender,
    UserInfoProp.Signature, UserInfoProp.Tel)

  def getUserInfo(userId: Long) = Action.async(request => {

    val selfId = for {
      data <- request.body.asJson
      v <- (data \ "UserId").asOpt[Long]
    } yield v

    val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
    formatter.setSelfView((selfId getOrElse 0) == userId)

    val future: Future[Result] = try {
      FinagleFactory.client.getUserById(userId, Some(basicUserInfoFieds)) map (user => {
        val node = formatter.formatNode(user).asInstanceOf[ObjectNode]

        // TODO 缺少接口 获得其他用户属性
        node.put("guideCnt", 0) // GuideAPI.getGuideCntByUser(userId))
        node.set("tracks", new ObjectMapper().createArrayNode())
        node.set("travelNotes", new ObjectMapper().createArrayNode())

        Utils.status(node.toString).toScala
      })
    } catch {
      case _: NotFoundException =>
        Future {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }

    future
  })


  def signup() = Action.async(request => {
    val data = for {
      data <- request.body.asJson
      pwd <- (data \ "pwd").asOpt[String]
      captcha <- (data \ "captcha").asOpt[String]
      tel <- (data \ "tel").asOpt[String] map PhoneParserFactory.newInstance().parse
    } yield {
        if (captcha == "85438734" || UserAPI.checkValidation(tel.getDialCode, tel.getPhoneNumber, 1, captcha, null)) {
          val nickName = "旅行派_" + tel.getPhoneNumber
          val future: Future[Result] = FinagleFactory.client.createUser(nickName, pwd, Some(Map(UserInfoProp.Tel -> tel.getPhoneNumber))) map (user => {
            val node = new UserFormatterOld(true).format(user)
            Utils.createResponse(ErrorCode.NORMAL, node).toScala
          }) rescue {
            case _: UserExistsException =>
              TwitterFuture {
                Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true).toScala
              }
          }
          future
        }
        else
          Future {
            Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true).toScala
          }
      }
    data.get
  })
}
