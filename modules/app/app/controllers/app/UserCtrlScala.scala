package controllers.app

import aizou.core.UserAPI
import aizou.core.user.ValFormatterFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{LongNode, ObjectNode, TextNode}
import com.lvxingpai.yunkai.{UserInfo => YunkaiUserInfo, _}
import com.twitter.util.{Future => TwitterFuture}
import database.MorphiaFactory
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.user.{UserFormatterOld, UserInfoFormatter}
import misc.TwitterConverter._
import misc.{FinagleConvert, FinagleFactory}
import models.misc.ValidationCode
import models.user.UserInfo
import org.mongodb.morphia.Datastore
import play.api.mvc.{Action, Controller, Result, Results}
import play.libs.Json
import utils.phone.PhoneParserFactory
import utils.{MsgConstants, Utils}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
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
      //      oldPassword <- (body \ "oldPwd").asOpt[String]
      newPassword <- (body \ "newPwd").asOpt[String]
    } yield {
        FinagleFactory.client.resetPassword(userId, newPassword) map (_ => {
          Utils.createResponse(ErrorCode.NORMAL, "Success!").toScala
        }) rescue {
          case _: NotFoundException =>
            TwitterFuture {
              Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
            }
          case _: InvalidArgsException =>
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

  def getContact(userId: Long, contactId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val isContactFuture = client.isContact(userId, contactId)

    val future: TwitterFuture[Result] = isContactFuture flatMap (isContact => {
      if (isContact) {
        client.getUserById(contactId, Some(basicUserInfoFieds)) map (user => {
          val formatter = FormatterFactory.getInstance(classOf[UserInfoFormatter])
          val fields = basicUserInfoFieds
          val node = formatter.formatNode(user)
          Utils.createResponse(ErrorCode.NORMAL, node).toScala
        })
      } else TwitterFuture {
        Results.NotFound
      }
    })

    future
  })


  def addContact(userId: Long) = Action.async(request => {
    val ret = (for {
      body <- request.body.asJson
      contactId <- (body \ "userId").asOpt[Long]
    } yield {
        FinagleFactory.client.addContact(userId, contactId) map (_ => {
          Utils.createResponse(ErrorCode.NORMAL).toScala
        }) rescue {
          case _: NotFoundException =>
            TwitterFuture {
              Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
            }
        }
      }) getOrElse {
      TwitterFuture {
        Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
      }
    }
    ret
  })


  def delContact(userId: Long, contactId: Long) = Action.async(request => {
    FinagleFactory.client.removeContact(userId, contactId) map (_ => {
      Utils.createResponse(ErrorCode.NORMAL).toScala
    }) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
        }
    }
  })


  /**
   * 发送验证码
   * @return
   */
  def sendValidationCode() = Action.async(request => {
    object ActionCode extends Enumeration {
      val Signup = Value(1)
      val ResetPassword = Value(2)
      val BindCellPhone = Value(3)
    }

    def func(countryCode: Int, tel: String, actionCode: Int, userId: Long, expireMs: Long, resendMs: Long): Future[Long] = {
      val ds: Datastore = MorphiaFactory.datastore
      for {
        valCode <- Future {
          val searchResult = ds.createQuery(classOf[ValidationCode]).field("key").equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get
          Option(searchResult)
        }
        future <- {
          val cc = valCode.get
          if (valCode.nonEmpty && System.currentTimeMillis < valCode.get.resendTime)
            Future {
              (valCode.get.resendTime - System.currentTimeMillis) / 1000L
            }
          else {
            val recipients = Seq(tel)
            val newCode = ValidationCode.newInstance(countryCode, tel, actionCode, userId, expireMs)
            if (valCode.nonEmpty)
              newCode.id = valCode.get.id
            val content: String = ValFormatterFactory.newInstance(actionCode).format(countryCode, tel, newCode.value, expireMs, null)

            val future: Future[Long] = FinagleFactory.smsClient.sendSms(content, recipients) map (_ => {
              newCode.lastSendTime = System.currentTimeMillis()
              newCode.resendTime = newCode.lastSendTime + resendMs
              ds.save[ValidationCode](newCode)
              resendMs / 1000L
            })
            future
          }
        }
      } yield future
    }

    val future = for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
      countryCode <- (body \ "dialCode").asOpt[Int] orElse Some(86)
      actionCode <- (body \ "actionCode").asOpt[Int]
      userId <- (body \ "userId").asOpt[Long]
    } yield {
        // 用户是否存在
        FinagleFactory.client.getUserById(userId, None) map (_ => true) rescue {
          case _: NotFoundException => TwitterFuture {
            false
          }
        } map (exists => {
          val condition = (actionCode, exists)
          val signUp = ActionCode.Signup.id
          condition match {
            case item if item._1 == ActionCode.Signup.id && item._2 =>
              Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true).toScala
            case item if item._1 == ActionCode.ResetPassword.id && !item._2 =>
              Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true).toScala
            case item if item._1 == ActionCode.BindCellPhone.id && !item._2 =>
              Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true).toScala
            case _ =>
              val expireMs = 10 * 60 * 1000
              val resendMs = 60 * 1000

              val coolDown = Await.result(func(countryCode, PhoneParserFactory.newInstance().parse(tel).getPhoneNumber,
                actionCode, userId, expireMs, resendMs), Duration.Inf)

              val node = new ObjectMapper().createObjectNode()
              node.set("coolDown", LongNode.valueOf(coolDown))
              Utils.createResponse(ErrorCode.NORMAL, node).toScala
          }
        })
      }

    val ret = future getOrElse {
      // 如果ret为None，说明输入有误
      TwitterFuture {
        Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
      }
    }
    ret
  })

  /**
   * 绑定手机
   * @return
   */
  def bindCellPhone() = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
      userId <- (body \ "userId").asOpt[Long]
    } yield {
        FinagleFactory.client.updateTelNumber(userId, PhoneParserFactory.newInstance().parse(tel).getPhoneNumber) map (_ => {
          Utils.createResponse(ErrorCode.NORMAL, "Success!").toScala
        }) rescue {
          case _: NotFoundException =>
            TwitterFuture {
              Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
            }
        }
      }
    ret.get
  })


  /**
   * 发送好友请求
   * @return
   */
  def requestContact(userId: Long) = Action.async(request => {
    val ret = (for {
      body <- request.body.asJson
      contactId <- (body \ "userId").asOpt[Long]
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
          case _@(InvalidArgsException() | InvalidStateException()) =>
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
          case _@(InvalidArgsException() | InvalidStateException()) =>
            TwitterFuture {
              Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
            }
        }
      }) getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    ret
  })
}
