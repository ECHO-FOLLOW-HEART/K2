package controllers.app

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.{ArrayNode, LongNode, ObjectNode, TextNode}
import com.fasterxml.jackson.databind.{JsonSerializer, ObjectMapper, SerializerProvider}
import com.lvxingpai.yunkai.{UserInfo => YunkaiUserInfo, _}
import com.twitter.util.{Future => TwitterFuture}
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.user.{UserFormatterOld, UserInfoFormatter}
import misc.TwitterConverter._
import misc.{FinagleConvert, FinagleFactory}
import models.user.UserInfo
import play.api.mvc.{Action, Controller, Result, Results}
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
      password <- (body \ "password").asOpt[String]
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

    val future = ret getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    future
  })

  def signup() = Action.async(request => {
    val client = FinagleFactory.client
    val action = 1 // 创建用户

    /**
     * 验证码的有效性
     */
    def checkCode(valCode: String, tel: String, countryCode: Option[Int]): TwitterFuture[Boolean] = {
      val magicCode = "85438734"

      if (valCode == magicCode)
        TwitterFuture {
          true
        }
      else {
        val future = client.checkValidationCode(valCode, action, countryCode, tel, None) map (_ => true)
        future rescue {
          case _: ValidationCodeException => TwitterFuture {
            false
          }
        }
      }
    }

    val ret = for {
      body <- request.body.asJson
      password <- (body \ "password").asOpt[String] orElse (body \ "pwd").asOpt[String]
      valCode <- (body \ "captcha").asOpt[String] orElse (body \ "validationCode").asOpt[String]
      tel <- (body \ "tel").asOpt[String] map PhoneParserFactory.newInstance().parse
    } yield {
        checkCode(valCode, tel.getPhoneNumber, None) flatMap (checkCodeResult => {
          if (checkCodeResult) {
            val nickName = "旅行派_" + tel.getPhoneNumber
            FinagleFactory.client.createUser(nickName, password, Some(Map(UserInfoProp.Tel -> tel.getPhoneNumber))) map (user => {
              val node = new UserFormatterOld(true).format(user)
              Utils.createResponse(ErrorCode.NORMAL, node).toScala
            }) rescue {
              case _: UserExistsException =>
                TwitterFuture {
                  Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true).toScala
                }
            }
          } else
            TwitterFuture {
              Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true).toScala
            }
        })
      }

    ret get
  })

  def resetPassword(userId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val ret = for {
      body <- request.body.asJson
      newPassword <- (body \ "newPassword").asOpt[String]
    } yield {
        // 获得旧密码，或者token
        val oldPassword = (body \ "oldPassword").asOpt[String]
        val token = (body \ "token").asOpt[String]

        val resetFuture = {
          if (oldPassword.nonEmpty || token.nonEmpty) {
            val future = if (oldPassword.nonEmpty)
              client.resetPassword(userId, oldPassword.get, newPassword)
            else
              client.resetPasswordByToken(userId, token.get, newPassword)

            (future map (_ => {
              Utils.createResponse(ErrorCode.NORMAL).toScala
            })) rescue {
              case _: NotFoundException =>
                TwitterFuture {
                  Utils.createResponse(ErrorCode.USER_NOT_EXIST).toScala
                }
              case _: InvalidArgsException =>
                TwitterFuture {
                  Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
                }
            }
          } else TwitterFuture {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
        }

        resetFuture
      }

    val future = ret getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    future
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

    val future = for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
      countryCode <- (body \ "dialCode").asOpt[Int] orElse Some(86)
      actionCode <- (body \ "actionCode").asOpt[Int]
    } yield {
        // 用户是否存在
        // 如果没有userId，则返回false
        val userOpt = (body \ "userId").asOpt[Long]
        val userExistFuture = (for {
          userId <- userOpt
        } yield {
            FinagleFactory.client.getUserById(userId, None) map (_ => true) rescue {
              case _: NotFoundException => TwitterFuture {
                false
              }
            }
          }) getOrElse TwitterFuture {
          false
        }

        for {
          exists <- userExistFuture
          result <- {
            val condition = (actionCode, exists)
            condition match {
              case item if item._1 == ActionCode.ResetPassword.id && !item._2 => TwitterFuture {
                Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true).toScala
              }
              case item if item._1 == ActionCode.BindCellPhone.id && !item._2 => TwitterFuture {
                Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true).toScala
              }
              case _ =>
                val telEntry = PhoneParserFactory.newInstance().parse(tel)

                (FinagleFactory.client.sendValidationCode(actionCode, Some(countryCode), telEntry.getPhoneNumber, userOpt) map (_ => {
                  val node = new ObjectMapper().createObjectNode()
                  node.set("coolDown", LongNode.valueOf(60))
                  Utils.createResponse(ErrorCode.NORMAL, node).toScala
                })) rescue {
                  case _: InvalidStateException => TwitterFuture {
                    Utils.createResponse(ErrorCode.ILLEGAL_STATE).toScala
                  }
                }
            }
          }
        } yield result
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
  def bindCellPhone(userId: Long) = Action.async(request => {
    val ret = for {
      body <- request.body.asJson
      tel <- (body \ "tel").asOpt[String]
    //      userId <- (body \ "userId").asOpt[Long]
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

  def checkValidationCode(action: Int, code: String, userId: Int, tel: String, countryCode: Int) = play.mvc.Results.TODO

  def updateUserInfo(uid: Long) = play.mvc.Results.TODO

  def matchAddressBook(uid: Long) = play.mvc.Results.TODO
}
