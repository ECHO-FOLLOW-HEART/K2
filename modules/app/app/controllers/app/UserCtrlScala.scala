package controllers.app

import api.UserUgcAPI
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
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{ Action, Controller, Result }

import utils.Implicits._
import utils.phone.PhoneParserFactory
import utils.{ Result => K2Result, Utils }

import scala.collection.JavaConversions._
import scala.concurrent.{ Future => ScalaFuture }
import scala.language.{ implicitConversions, postfixOps }

/**
 * Created by zephyre on 6/30/15.
 */
object UserCtrlScala extends Controller {
  import UserInfoProp._

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  val basicUserInfoFieds = Seq(UserId, NickName, Avatar, Gender, Signature, Residence, Birthday)

  def getUserInfo(userId: Long) = Action.async(request => {

    val isSelf = request.headers.get("UserId").map(_.toLong).getOrElse(0) == userId

    val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
    val fields = basicUserInfoFieds ++ (if (isSelf) Seq(Tel) else Seq())
    formatter.setSelfView(isSelf)
    (for {
      user <- FinagleFactory.client.getUserById(userId, Some(fields), None)

      guideCnt <- UserUgcAPI.getGuidesCntByUser(user.getUserId)
      albumCnt <- UserUgcAPI.getAlbumsCntByUser(user.getUserId)
      trackCntAndCountryCnt <- UserUgcAPI.getTrackCntAndCountryCntByUser(user.getUserId)
    } yield {
      val node = formatter.formatNode(user).asInstanceOf[ObjectNode]
      node.put("guideCnt", guideCnt)
      node.put("trackCnt", trackCntAndCountryCnt._1)
      node.put("countryCnt", trackCntAndCountryCnt._2)
      node.put("travelNoteCnt", 0)
      node.put("albumCnt", albumCnt)
      Utils.status(node.toString).toScala
    }
    ) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }
  })

  /**
   * 注销
   * @return
   */
  def logout() = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
      userId <- (body \ "userId").asOpt[Long]
    } yield {
      TwitterFuture(K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  def login() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
    } yield {
      val password = (body \ "password").asOpt[String]
      val loginName = (body \ "loginName").asOpt[String]
      val authCode = (body \ "authCode").asOpt[String]
      val provider = (body \ "provider").asOpt[String]
      if (password.nonEmpty && loginName.nonEmpty) {
        val telEntry = PhoneParserFactory.newInstance().parse(loginName.get)
        val future = FinagleFactory.client.login(telEntry.getPhoneNumber, password.get, "app") map (user => {
          val userFormatter = new UserLoginFormatter(true)
          K2Result.ok(Some(userFormatter.format(user)))
        })
        future rescue {
          case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Invalid loginName/password"))
        }
      } else if (authCode.nonEmpty && provider.nonEmpty) {
        val future = FinagleFactory.client.loginByOAuth(authCode.get, provider.get) map (user => {
          val userFormatter = new UserLoginFormatter(true)
          K2Result.ok(Some(userFormatter.format(user)))
        })
        future rescue {
          case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Invalid authCode/authProvider"))
        }
      } else
        TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Lack of login information"))
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

  /**
   * 设置badge值
   */
  def setBadge(userId: Long) = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
      badge <- (body \ "badge").asOpt[Int]
    } yield {
      TwitterFuture(K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  /**
   * 通过token修改密码
   */
  def resetPasswordByToken() = Action.async(request => {
    val client = FinagleFactory.client

    val future = (for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
      newPassword <- (body \ "newPassword").asOpt[String]
      token <- (body \ "token").asOpt[String]
    } yield {
      val ret1 = (for {
        userList <- client.searchUserInfo(Map(UserInfoProp.Tel -> tel), None, None, None)
      } yield {
        userList.headOption map (u => client.resetPasswordByToken(u.userId, newPassword, token))
        K2Result.ok(None)
      }) rescue {
        case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
        case _: InvalidArgsException => TwitterFuture(K2Result.unprocessable)
        case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, ""))
      }
      ret1
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  /**
   * 通过旧密码重置密码
   * @return
   */
  def resetPassword(userId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val ret = for {
      body <- request.body.asJson
      newPassword <- (body \ "newPassword").asOpt[String]
      oldPassword <- (body \ "oldPassword").asOpt[String]
    } yield {
      client.resetPassword(userId, oldPassword, newPassword) map (_ => K2Result.ok(None)) rescue {
        case _: NotFoundException => TwitterFuture(K2Result.notFound(ErrorCode.USER_NOT_EXIST, ""))
        case _: InvalidArgsException => TwitterFuture(K2Result.unprocessable)
        case _: AuthException => TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, ""))
      }
    }
    val future = ret getOrElse TwitterFuture(K2Result.unprocessable)
    future
  })

  /**
   * 修改好友的备注信息
   *
   * @param selfId 被修改人
   * @return
   */
  def updateContactMemo(selfId: Long, contactId: Long) = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      memo <- (body \ "memo").asOpt[String]
    } yield {
      FinagleFactory.client.updateMemo(selfId, contactId, memo) map (_ => K2Result.ok(None)) rescue {
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

    val future = FinagleFactory.client.getContactList(realUserId.get, Some(basicUserInfoFieds ++ Seq(UserInfoProp.Memo)), None, None) map (userList => {
      val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
      formatter.setSelfView(false)

      val array = new ObjectMapper().createArrayNode()
      userList foreach (user => {
        array.add(formatter formatNode user)
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
        client.getUserById(contactId, Some(basicUserInfoFieds), Some(userId)) map (user => {
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

    def sendSignupValidationCode(tel: String): TwitterFuture[Result] = sendValidationCodesImpl(Signup, None, None, tel)

    def sendResetPassword(tel: String) = sendValidationCodesImpl(ResetPassword, None, None, tel)

    def sendOtherValidationCode(action: OperationCode, userId: Long, tel: String): TwitterFuture[Result] = {
      // 发送验证码。如果用户的tel不存在，则返回INVALID_ARGUMENT
      for {
        userOpt <- client.getUserById(userId, Some(Seq()), None)
        result <- {
          if (userOpt == null)
            TwitterFuture(K2Result(UNPROCESSABLE_ENTITY, ErrorCode.USER_NOT_EXIST, s"The user $userId does not exist"))
          else
            sendValidationCodesImpl(action, Some(userId), None, tel)
        }
      } yield result
    }

    // 真正发送验证码的代码
    def sendValidationCodesImpl(action: OperationCode, uid: Option[Long], countryCode: Option[Int], tel: String): TwitterFuture[Result] = {
      if (tel isEmpty)
        TwitterFuture(throw InvalidArgsException(Some("The phone number is invalid")))

      client.sendValidationCode(action, uid, tel, countryCode) map (_ => {
        val node = new ObjectMapper().createObjectNode()
        node.set("coolDown", LongNode.valueOf(60))
        K2Result.ok(Some(node))
      })
    }

    val ret = for {
      body <- request.body.asJson
      actionCode <- (body \ "action").asOpt[Int]
      userId <- (body \ "userId").asOpt[Long] orElse Option(-1L)
      tel <- (body \ "tel").asOpt[String] map (PhoneParserFactory.newInstance().parse(_).getPhoneNumber) orElse Some("")
    } yield {
      // 根据action code的不同，分别调用对应的操作
      (actionCode match {
        case item if item == OperationCode.Signup.value => sendSignupValidationCode(tel)
        case item if item == OperationCode.ResetPassword.value => sendResetPassword(tel)
        case item if item == OperationCode.UpdateTel.value => sendOtherValidationCode(UpdateTel, userId, tel)
        case _ =>
          TwitterFuture(K2Result(UNPROCESSABLE_ENTITY, ErrorCode.INVALID_ARGUMENT, s"Invalid action code: $actionCode"))
      }) rescue {
        case _: OverQuotaLimitException =>
          TwitterFuture(K2Result.forbidden(ErrorCode.SMS_QUOTA_ERROR, "Exceeds the SMS sending rate limit"))
        case _: InvalidArgsException =>
          TwitterFuture(K2Result.unprocessable)
        case _: ResourceConflictException =>
          TwitterFuture(K2Result.conflict(ErrorCode.USER_EXIST, s"The phone number $tel already exists"))
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

  def checkValidationCode() = Action.async(request => {
    val future = (for {
      body <- request.body.asJson
      action <- (body \ "action").asOpt[Int]
      code <- (body \ "validationCode").asOpt[String]
      tel <- (body \ "tel").asOpt[String]
    } yield {
      FinagleFactory.client.checkValidationCode(code, action, tel, None) map (token => {
        val node = new ObjectMapper().createObjectNode().set("token", TextNode.valueOf(token))
        Utils.createResponse(ErrorCode.NORMAL, node).toScala
      }) rescue {
        case _: ValidationCodeException =>
          TwitterFuture(K2Result.unauthorized(ErrorCode.AUTH_ERROR, "Invalid validation code"))
      }
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  def updateUserInfo(uid: Long) = Action.async(request => {
    import UserInfoProp._

    /**
     * 日期格式转换。将yyyy-MM-dd格式，转换为MM/dd/yyyy格式
     * @return
     */
    def dateFormatConvert(input: String): String = {
      val fmtInput = DateTimeFormat.forPattern("yyyy-MM-dd")
      val fmtOutput = DateTimeFormat.forPattern("MM/dd/yyyy")
      val date = fmtInput.parseDateTime(input)
      fmtOutput.print(date)
    }

    val client = FinagleFactory.client

    val future = (for {
      body <- request.body.asJson
    } yield {
      val nickNameOpt = (body \ "nickName").asOpt[String]
      val signatureOpt = (body \ "signature").asOpt[String]
      val avatar = (body \ "avatar").asOpt[String]
      val genderOpt = (body \ "gender").asOpt[String]
      val residenceOpt = (body \ "residence").asOpt[String]
      val birthdayOpt = (body \ "birthday").asOpt[String] map dateFormatConvert
      val updateMap: Map[UserInfoProp, String] = Map(NickName -> nickNameOpt, Signature -> signatureOpt,
        Avatar -> avatar, Gender -> genderOpt, Residence -> residenceOpt,
        Birthday -> birthdayOpt) filter (_._2.nonEmpty) map (v => (v._1, v._2.get))

      val ret = if (updateMap nonEmpty) {
        client.updateUserInfo(uid, updateMap) map (_ => ())
      } else
        TwitterFuture(())

      ret map (_ => K2Result.ok(None))
    }) getOrElse TwitterFuture(K2Result.unprocessable)

    future
  })

  def matchAddressBook(uid: Long) = play.mvc.Results.TODO

  /**
   * 搜索用户
   * @return
   */
  def searchUser(tel: Option[String] = None, nickName: Option[String] = None, userId: Option[Long] = None,
    query: Option[String],
    fields: Option[String] = None, offset: Int, limit: Int) = Action.async(request => {
    val client = FinagleFactory.client

    // 如果指定了query，则优先使用query的值
    val querySet = query map (v => {
      val userIdOpt = try {
        Some(v.toLong)
      } catch {
        case _: NumberFormatException => None
      }
      (Some(v), Some(v), userIdOpt)
    }) getOrElse (tel, nickName, userId)

    // 通过UserId进行搜索
    val future1 = querySet._3 map (v => {
      client.getUserById(v, Some(basicUserInfoFieds), None) map (Some(_)) rescue {
        case _: NotFoundException => TwitterFuture(None)
      }
    }) getOrElse TwitterFuture(None)

    // 通过其它字段进行搜索
    val queryMap: Map[UserInfoProp, String] = Map(UserInfoProp.Tel -> querySet._1,
      UserInfoProp.NickName -> querySet._2) filter (_._2.nonEmpty) map (v => (v._1, v._2.get))
    val future2 = FinagleFactory.client.searchUserInfo(queryMap, Some(basicUserInfoFieds), None, None)

    val ret = for {
      userOpt <- future1
      userSeq <- future2
    } yield {
      if (userOpt nonEmpty) {
        val userIdSet = userSeq map (_.userId) toSet
        val user = userOpt.get
        if (userIdSet contains user.userId)
          userSeq
        else
          userSeq :+ user
      } else
        userSeq
    }

    val future = ret map (userSeq => {
      val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
      formatter.setSelfView(false)

      val mapper = new ObjectMapper()
      val searchResult = mapper.createArrayNode()

      userSeq foreach (user => {
        val node = formatter.formatNode(user).asInstanceOf[ObjectNode]
        val guideCnt = 0
        val albumCnt = 0
        node.put("guideCnt", guideCnt)
        node.put("trackCnt", 0)
        node.put("travelNoteCnt", 0)
        node.put("albumCnt", albumCnt)

        searchResult.add(node)
      })

      K2Result.ok(Some(searchResult))
    })

    future
  })

  def setUserMemo(uid: Long, contactId: Long) = play.mvc.Results.TODO

  def getUsersInfoValue(userIds: java.util.List[java.lang.Long]): java.util.Map[java.lang.Long, UserInfo] = {
    val f = FinagleFactory.client.getUsersById(userIds.map(scala.Long.unbox(_)), Some(basicUserInfoFieds), None) map (userMap => {
      for {
        (k, v) <- userMap
      } yield (scala.Long.box(k), userInfoYunkai2Model(v))
    })
    f.toJavaFuture.get()
  }

}
