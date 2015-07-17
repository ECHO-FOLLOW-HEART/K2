package controllers.app

import aizou.core.UserUgcAPIScala
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.{ ArrayNode, LongNode, ObjectNode, TextNode }
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.lvxingpai.yunkai.{ UserInfo => YunkaiUserInfo, _ }
import com.twitter.util.{ Future => TwitterFuture }
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.user.{ UserInfoFormatter, UserLoginFormatter }
import misc.Implicits._
import misc.TwitterConverter._
import misc.{ FinagleConvert, FinagleFactory }
import models.user.UserInfo
import play.api.mvc.{ Action, Controller, Result }
import utils.phone.PhoneParserFactory
import utils.{ Result => K2Result, Utils }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future => ScalaFuture }
import scala.language.{ implicitConversions, postfixOps }

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
    (for {
      user <- FinagleFactory.client.getUserById(userId, Some(fields))
      guideCnt <- UserUgcAPIScala.getGuidesCntByUser(user.getUserId)
      albumCnt <- UserUgcAPIScala.getAlbumsCntByUser(user.getUserId)
    } yield ({
      val node = formatter.formatNode(user).asInstanceOf[ObjectNode]
      node.put("guideCnt", guideCnt)
      node.put("trackCnt", 0)
      node.put("travelNoteCnt", 0)
      node.put("albumCnt", albumCnt)
      Utils.status(node.toString).toScala
    })
    ) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }

  })

  def login() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      password <- (body \ "password").asOpt[String]
      loginName <- (body \ "loginName").asOpt[String]
    } yield {
      val telEntry = PhoneParserFactory.newInstance().parse(loginName)
      val future = FinagleFactory.client.login(telEntry.getPhoneNumber, password, "app") map (user => {
        val userFormatter = new UserLoginFormatter(true)
        K2Result.ok(Some(userFormatter.format(user)))
      })
      future rescue {
        case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Invalid loginName/password"))
      }
    }

    val future = ret getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  def signup() = Action.async(request => {
    val client = FinagleFactory.client
    val action = OperationCode.Signup

    val ret = for {
      body <- request.body.asJson
      password <- (body \ "password").asOpt[String] orElse (body \ "pwd").asOpt[String]
      valCode <- (body \ "captcha").asOpt[String] orElse (body \ "validationCode").asOpt[String]
      tel <- (body \ "tel").asOpt[String] map PhoneParserFactory.newInstance().parse
    } yield {
      client.checkValidationCode(valCode, action, tel.getPhoneNumber, None) flatMap (_ => {
        val nickName = "旅行派_" + tel.getPhoneNumber
        FinagleFactory.client.createUser(nickName, password, Some(Map(UserInfoProp.Tel -> tel.getPhoneNumber))) map (user => {
          val node = new UserLoginFormatter(true).format(user)
          K2Result.created(Some(node))
        })
      }) rescue {
        case _: ResourceConflictException =>
          TwitterFuture(K2Result.conflict(ErrorCode.USER_EXIST, "Already exists"))
        case _: ValidationCodeException =>
          TwitterFuture(K2Result.unauthorized(ErrorCode.CAPTCHA_ERROR, "The validation code is invalid"))
        case _: InvalidArgsException =>
          TwitterFuture(K2Result.unprocessable)
      }
    }

    val future = ret getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  def resetPassword(userId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val ret = for {
      body <- request.body.asJson
      newPassword <- (body \ "newPassword").asOpt[String]
      oldPassword <- (body \ "oldPassword").asOpt[String] orElse Some("")
      token <- (body \ "token").asOpt[String] orElse Some("")
    } yield {
      // 获得旧密码，或者token
      ((oldPassword, token) match {
        case item if item._1 nonEmpty =>
          client.resetPassword(userId, oldPassword, newPassword)
        case item if item._2 nonEmpty =>
          client.resetPasswordByToken(userId, newPassword, token)
        case _ =>
          throw InvalidArgsException()
      }) map (_ => K2Result.ok(None)) rescue {
        case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
        case _: InvalidArgsException => TwitterFuture(K2Result.unprocessable)
        case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, ""))
      }
    }

    val future = ret getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  def getContactList(userId: Long) = Action.async(request => {
    val realUserId =
      if (userId == 0)
        request.headers.get("UserId") map (_.toLong)
      else
        Some(userId)

    val future = FinagleFactory.client.getContactList(realUserId.get, Some(basicUserInfoFieds), None, None) map (userList => {
      val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
      formatter.setSelfView(false)

      val array = new ObjectMapper().createArrayNode()
      userList foreach (user => {
        val u: UserInfo = user
        u.setMemo("")
        array.add(formatter formatNode u)
      })

      val node = new ObjectMapper().createObjectNode()
      node.set("contacts", array)
      K2Result.ok(Some(node))
    }) rescue {
      case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
    }

    future
  })

  def getContact(userId: Long, contactId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val isContactFuture = client.isContact(userId, contactId)

    val future: TwitterFuture[Result] = isContactFuture flatMap (isContact => {
      if (isContact) {
        client.getUserById(contactId, Some(basicUserInfoFieds)) map (user => {
          val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
          val node = formatter.formatNode(user)
          K2Result.ok(Some(node))
        })
      } else TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
    })

    future
  })

  def addContact(userId: Long) = Action.async(request => {
    val ret = (for {
      body <- request.body.asJson
      contactId <- (body \ "userId").asOpt[Long]
    } yield {
      FinagleFactory.client.addContact(userId, contactId) map (_ => K2Result.ok(None)) rescue {
        case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
      }
    }) getOrElse TwitterFuture(K2Result.unprocessable)
    ret
  })

  def delContact(userId: Long, contactId: Long) = Action.async(request => {
    val future = FinagleFactory.client.removeContact(userId, contactId) map (_ => K2Result.ok(None)) rescue {
      case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
    }
    future
  })

  /**
   * 发送验证码
   * @return
   */
  def sendValidationCode() = Action.async(request => {
    import com.lvxingpai.yunkai.OperationCode._

    val client = FinagleFactory.client

    def sendSignupValidationCode(tel: String): TwitterFuture[Result] = sendValidationCodesImpl(Signup, None, tel)

    def sendResetPassword(tel: String) = sendValidationCodesImpl(ResetPassword, None, tel)

    def sendOtherValidationCode(action: OperationCode, userId: Long): TwitterFuture[Result] = {
      // 发送验证码。如果用户的tel不存在，则返回INVALID_ARGUMENT
      for {
        telOpt <- client.getUserById(userId, Some(Seq(UserInfoProp.Tel))) map (_.tel)
        result <- {
          if (telOpt isEmpty)
            TwitterFuture(K2Result(UNPROCESSABLE_ENTITY, ErrorCode.USER_NOT_EXIST, s"The user $userId does not exist"))
          else
            sendValidationCodesImpl(action, None, telOpt.get)
        }
      } yield result
    }

    // 真正发送验证码的代码
    def sendValidationCodesImpl(action: OperationCode, countryCode: Option[Int], tel: String): TwitterFuture[Result] = {
      if (tel isEmpty)
        TwitterFuture(throw InvalidArgsException(Some("The phone number is invalid")))

      client.sendValidationCode(action, tel, countryCode) map (_ => {
        val node = new ObjectMapper().createObjectNode()
        node.set("coolDown", LongNode.valueOf(60))
        K2Result.ok(Some(node))
      })
    }

    val ret = for {
      body <- request.body.asJson
      actionCode <- (body \ "actionCode").asOpt[Int]
      userId <- (body \ "userId").asOpt[Long] orElse Option(-1L)
      tel <- (body \ "tel").asOpt[String] map (PhoneParserFactory.newInstance().parse(_).getPhoneNumber) orElse Some("")
    } yield {
      // 根据action code的不同，分别调用对应的操作
      (actionCode match {
        case item if item == OperationCode.Signup.value => sendSignupValidationCode(tel)
        case item if item == OperationCode.ResetPassword.value => sendResetPassword(tel)
        case item if item == OperationCode.UpdateTel.value => sendOtherValidationCode(UpdateTel, userId)
        case _ =>
          TwitterFuture(K2Result(UNPROCESSABLE_ENTITY, ErrorCode.INVALID_ARGUMENT, s"Invalid action code: $actionCode"))
      }) rescue {
        case _: OverQuotaLimitException =>
          TwitterFuture(K2Result.forbidden(ErrorCode.SMS_QUOTA_ERROR, "Exceeds the SMS sending rate limit"))
        case _: InvalidArgsException =>
          TwitterFuture(K2Result.unprocessable)
        case _: NotFoundException =>
          TwitterFuture(K2Result(UNPROCESSABLE_ENTITY, ErrorCode.USER_NOT_EXIST, s"The user $userId does not exist"))
      }
    }

    val future = ret getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  /**
   * 绑定手机
   * @return
   */
  def bindCellPhone(userId: Long) = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String] map (PhoneParserFactory.newInstance().parse(_).getPhoneNumber)
      token <- (body \ "token").asOpt[String]
    } yield {
      FinagleFactory.client.updateTelNumber(userId, tel, token) map (_ => K2Result.ok(None)) rescue {
        case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
        case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, ""))
        case _: InvalidArgsException => TwitterFuture(K2Result.unprocessable)
        case _: ResourceConflictException =>
          TwitterFuture(K2Result.conflict(ErrorCode.INVALID_ARGUMENT, s"Phone number $tel already exists"))
      }
    }
    ret.get
  })

  /**
   * 获得好友请求的列表
   * @param userId
   * @return
   */
  def getContactRequests(userId: Long, offset: Int, limit: Int) = Action.async(request => {
    val ret = FinagleFactory.client.getContactRequests(userId, Option(offset), Option(limit)) map (reqSeq => {
      val mapper = new ObjectMapper()
      val module = new SimpleModule()

      val ser = new JsonSerializer[ContactRequest] {
        override def serialize(value: ContactRequest, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
          gen.writeStartObject()

          gen.writeStringField("id", value.id)
          gen.writeNumberField("sender", value.sender)
          gen.writeNumberField("receiver", value.receiver)
          gen.writeStringField("status", value.status match {
            case 0 => "PENDING"
            case 1 => "ACCEPTED"
            case 2 => "REJECTED"
            case 3 => "CANCELLED"
            case _ => "UNKNOWN"
          })
          gen.writeStringField("requestMessage", Option(value.requestMessage) getOrElse "")
          gen.writeStringField("rejectMessage", Option(value.rejectMessage) getOrElse "")
          gen.writeNumberField("timestamp", value.timestamp)
          gen.writeNumberField("expire", value.expire)

          gen.writeEndObject()
        }
      }
      module.addSerializer(classOf[ContactRequest], ser)
      mapper.registerModule(module)

      val node = mapper.valueToTree[ArrayNode](bufferAsJavaList(reqSeq.toBuffer))
      Utils.createResponse(ErrorCode.NORMAL, node).toScala
    }) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }

    ret
  })

  /**
   * 发送好友请求
   * @return
   */
  def requestContact(userId: Long) = Action.async(request => {
    val ret = (for {
      body <- request.body.asJson
      contactId <- (body \ "contactId").asOpt[Long]
    } yield {
      val message = (body \ "message").asOpt[String]
      FinagleFactory.client.sendContactRequest(userId, contactId, message) map (requestId => {
        val node = new ObjectMapper().createObjectNode()
        node.set("requestId", TextNode.valueOf(requestId))
        Utils.createResponse(ErrorCode.NORMAL, node).toScala
      }) rescue {
        case _: NotFoundException =>
          TwitterFuture {
            Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
          }
        case _@ (InvalidArgsException() | InvalidStateException()) =>
          TwitterFuture {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
      }
    }) getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    ret
  })

  /**
   * 接受或拒绝好友请求
   * @param userId
   * @param requestId
   * @return
   */
  def updateContactRequest(userId: Long, requestId: String) = Action.async(request => {
    object ActionCode extends Enumeration {
      val ACCEPT = Value(1)
      val REJECT = Value(2)
    }

    val ret = (for {
      body <- request.body.asJson
      action <- (body \ "action").asOpt[Int]
    } yield {
      val client = FinagleFactory.client
      val message = (body \ "message").asOpt[String]

      val func = action match {
        case item if item == ActionCode.ACCEPT.id => () => client.acceptContactRequest(requestId)
        case item if item == ActionCode.REJECT.id => () => client.rejectContactRequest(requestId, message)
      }

      func() map (_ => {
        Utils.createResponse(ErrorCode.NORMAL).toScala
      }) rescue {
        case _: NotFoundException =>
          TwitterFuture {
            Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
          }
        case _@ (InvalidArgsException() | InvalidStateException()) =>
          TwitterFuture {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
      }
    }) getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    ret
  })

  def checkValidationCode(action: Int, code: String, tel: String, countryCode: Option[Int]) = Action.async(request => {
    val future = FinagleFactory.client.checkValidationCode(code, action, tel, countryCode) map (token => {
      val node = new ObjectMapper().createObjectNode().set("token", TextNode.valueOf(token))
      Utils.createResponse(ErrorCode.NORMAL, node).toScala
    }) rescue {
      case _: ValidationCodeException =>
        TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Invalid validation code"))
    }
    future
  })

  def updateUserInfo(uid: Long) = play.mvc.Results.TODO

  def matchAddressBook(uid: Long) = play.mvc.Results.TODO

  def searchUser(tel: Option[String] = None, nickName: Option[String] = None, fields: Option[String] = None, offset: Int, limit: Int) = play.mvc.Results.TODO

  def setUserMemo(uid: Long, contactId: Long) = play.mvc.Results.TODO

  def getUsersInfoValue(userIds: java.util.List[java.lang.Long]): java.util.Map[java.lang.Long, UserInfo] = {
    val f = FinagleFactory.client.getUsersById(userIds.map(scala.Long.unbox(_)), Some(basicUserInfoFieds)) map (userMap => {
      for {
        (k, v) <- userMap
      } yield (scala.Long.box(k), userInfoYunkai2Model(v))
    })
    f.toJavaFuture.get()
  }

}
