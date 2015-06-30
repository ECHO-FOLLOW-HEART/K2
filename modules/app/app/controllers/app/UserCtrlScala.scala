package controllers.app

import aizou.core.UserAPI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.yunkai.{UserInfo => YunkaiUserInfo, _}
import com.twitter.util.{Future => TwitterFuture}
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.user.{UserFormatterOld, UserInfoFormatter}
import misc.TwitterConverter._
import misc.{FinagleConvert, FinagleFactory}
import models.user.UserInfo
import play.api.mvc.{Action, Controller}
import play.libs.Json
import utils.phone.PhoneParserFactory
import utils.{MsgConstants, Utils}

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}

/**
 * Created by zephyre on 6/30/15.
 */
object UserCtrlScala extends Controller {

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  val basicUserInfoFieds = Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar, UserInfoProp.Gender,
    UserInfoProp.Signature)

  def getUserInfo(userId: Long) = Action.async(request => {
    val isSelf = request.headers.get("UserId").map(_.toLong).getOrElse(0) == userId

    val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
    val fields = basicUserInfoFieds ++ (if (isSelf) Seq(UserInfoProp.Tel) else Seq())
    formatter.setSelfView(isSelf)

    (FinagleFactory.client.getUserById(userId, Some(fields)) map (user => {
      val node = formatter.formatNode(user).asInstanceOf[ObjectNode]

      // TODO 缺少接口 获得其他用户属性
      node.put("guideCnt", 0) // GuideAPI.getGuideCntByUser(userId))
      node.set("tracks", new ObjectMapper().createArrayNode())
      node.set("travelNotes", new ObjectMapper().createArrayNode())

      Utils.status(node.toString).toScala
    })) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }
  })

  def login() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      password <- (body \ "pwd").asOpt[String]
      loginName <- (body \ "loginName").asOpt[String]
    } yield {
        val telEntry = PhoneParserFactory.newInstance().parse(loginName)
        val future = FinagleFactory.client.login(telEntry.getPhoneNumber, password, "app") map (user => {
          val userFormatter = new UserFormatterOld(true)
          Utils.createResponse(ErrorCode.NORMAL, userFormatter.format(user)).toScala
        })
        future rescue {
          case _: AuthException =>
            TwitterFuture {
              Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true).toScala
            }
        }
      }
    ret get
  })

  def signup() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      pwd <- (body \ "pwd").asOpt[String]
      captcha <- (body \ "captcha").asOpt[String]
      tel <- (body \ "tel").asOpt[String] map PhoneParserFactory.newInstance().parse
    } yield {
        if (captcha == "85438734" || UserAPI.checkValidation(tel.getDialCode, tel.getPhoneNumber, 1, captcha, null)) {
          val nickName = "旅行派_" + tel.getPhoneNumber
          FinagleFactory.client.createUser(nickName, pwd, Some(Map(UserInfoProp.Tel -> tel.getPhoneNumber))) map (user => {
            val node = new UserFormatterOld(true).format(user)
            Utils.createResponse(ErrorCode.NORMAL, node).toScala
          }) rescue {
            case _: UserExistsException =>
              TwitterFuture {
                Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true).toScala
              }
          }
        }
        else
          TwitterFuture {
            Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true).toScala
          }
      }
    ret get
  })

  def resetPassword() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      userId <- (body \ "userId").asOpt[Long]
      oldPassword <- (body \ "oldPwd").asOpt[String]
      newPassword <- (body \ "newPwd").asOpt[String]
    } yield {
      FinagleFactory.client.resetPassword(userId, newPassword) map (_ => {
        Utils.createResponse(ErrorCode.NORMAL, "Success!").toScala
      }) rescue {
        case _:NotFoundException=>
          TwitterFuture {
            Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
          }
        case _:InvalidArgsException=>
          TwitterFuture {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
      }
    }
    ret get
  })

  def getContactList(userId: Long) = Action.async(request => {
    val realUserId =
      if (userId == 0)
        request.headers.get("UserId") map (_.toLong)
      else
        Some(userId)

    FinagleFactory.client.getContactList(realUserId.get, Some(basicUserInfoFieds), None, None) map (userList => {
      val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
      formatter.setSelfView(false)
      val nodeList = userList map (user => {
        val u: UserInfo = user
        u.setMemo("临时备注信息")
        formatter formatNode u
      })

      val node = new ObjectMapper().createObjectNode()
      node.set("contacts", Json.toJson(seqAsJavaList(nodeList)))
      Utils.createResponse(ErrorCode.NORMAL, node).toScala
    }) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }
  })
}
