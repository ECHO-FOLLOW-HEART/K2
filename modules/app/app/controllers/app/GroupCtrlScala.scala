package controllers.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ ArrayNode, ObjectNode }
import com.lvxingpai.yunkai.{ ChatGroup => YunkaiChatGroup, ChatGroupProp, NotFoundException, UserInfo => YunkaiUserInfo }
import com.twitter.util.{ Future => TwitterFuture }
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.group.ChatGroupFormatter
import formatter.taozi.user.UserInfoSimpleFormatter
import misc.TwitterConverter._
import misc.{ FinagleConvert, FinagleFactory }
import models.group.ChatGroup
import models.user.UserInfo
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ Action, Controller, Result }
import utils.Utils

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.language.{ implicitConversions, postfixOps }

/**
 * Created by zephyre on 6/30/15.
 */
object GroupCtrlScala extends Controller {

  implicit def groupInfoYunkai2Model(groupInfo: YunkaiChatGroup): ChatGroup = FinagleConvert.convertK2ChatGroup(groupInfo)

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  val basicChatGroupFieds = Seq(ChatGroupProp.ChatGroupId, ChatGroupProp.Creator, ChatGroupProp.Name, ChatGroupProp.Visible, ChatGroupProp.Avatar, ChatGroupProp.GroupDesc)
  val ACTION_ADDMEMBERS = "addMembers"
  val ACTION_DELMEMBERS = "delMembers"

  /**
   * 取得群组信息
   *
   * @param gID
   * @return
   */
  def getGroup1(gID: Long) = Action.async {
    request =>
      {
        val future: Future[Result] = try {
          FinagleFactory.client.getChatGroup(gID, Some(Seq())) map (chatGroup => {
            val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
            val node = formatter.formatNode(chatGroup).asInstanceOf[ObjectNode]
            Utils.status(node.toString).toScala
          })
        } catch {
          case _: NotFoundException =>
            Future {
              Utils.createResponse(ErrorCode.DATA_NOT_EXIST).toScala
            }
        }
        future
      }
  }

  def getGroup(gid: Long) = Action.async(request => {
    FinagleFactory.client.getChatGroup(gid, Some(basicChatGroupFieds)) map (chatGroup => {
      val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
      val node = formatter.formatNode(chatGroup).asInstanceOf[ObjectNode]
      Utils.createResponse(ErrorCode.NORMAL, node).toScala
    }) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
        }
    }
  })

  /**
   * 创建群组
   *
   * @return
   */
  def createGroup() = Action.async {
    request =>
      {
        val uid = request.headers.get("UserId").get.toLong
        val jsonNode = request.body.asJson.get

        val name = (jsonNode \ "name").asOpt[String].getOrElse("旅行派讨论组")
        val avatar = (jsonNode \ "avatar").asOpt[String].getOrElse("")
        val desc = (jsonNode \ "desc").asOpt[String].getOrElse("")
        val participants = (jsonNode \ "members").asOpt[Array[Long]]
        val participantsValue = participants.getOrElse(Array.emptyLongArray).toSeq
        val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
        val propMap: Map[ChatGroupProp, String] = Map(ChatGroupProp.Name -> name, ChatGroupProp.Avatar -> avatar, ChatGroupProp.GroupDesc -> desc)

        (FinagleFactory.client.createChatGroup(uid, participantsValue, Some(propMap)) map (chatGroup => {
          val node = formatter.formatNode(chatGroup).asInstanceOf[ObjectNode]
          Utils.status(node.toString).toScala
        })) rescue {
          case _: NotFoundException =>
            Future {
              Utils.createResponse(ErrorCode.DATA_NOT_EXIST).toScala
            }
        }
      }
  }

  /**
   * 修改群组
   *
   * @param gid
   * @return
   */
  def modifyGroup(gid: Long) = Action.async {
    request =>
      {
        val uid = request.headers.get("UserId").get.toLong
        val jsonNode = request.body.asJson.get
        val name = (jsonNode \ "name").asOpt[String]
        val desc = (jsonNode \ "desc").asOpt[String]
        val avatar = (jsonNode \ "avatar").asOpt[String]

        val operatorId = request.headers.get("UserId").get.toLong

        val propMap = scala.collection.mutable.Map[ChatGroupProp, String]()
        if (name.nonEmpty)
          propMap.put(ChatGroupProp.Name, name.get)
        if (desc.nonEmpty)
          propMap.put(ChatGroupProp.GroupDesc, desc.get)
        if (avatar.nonEmpty)
          propMap.put(ChatGroupProp.Avatar, avatar.get)

        val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
        (FinagleFactory.client.updateChatGroup(gid, operatorId, propMap) map (chatGroup => {
          val node = formatter.formatNode(chatGroup).asInstanceOf[ObjectNode]
          Utils.status(node.toString).toScala
        })) rescue {
          case _: NotFoundException =>
            Future {
              Utils.createResponse(ErrorCode.DATA_NOT_EXIST).toScala
            }
        }
      }
  }

  /**
   * 取得群组中的成员信息
   *
   * @param gid
   * @return
   */
  def getGroupUsers(gid: Long) = Action.async {
    request =>
      {

        val selfId = request.headers.get("UserId").get.toLong
        val formatter = FormatterFactory.getInstance(classOf[UserInfoSimpleFormatter])
        (FinagleFactory.client.getChatGroupMembers(gid, Some(UserCtrlScala.basicUserInfoFieds), Some(selfId)) map (users => {
          val usersList = users map (user => {
            UserCtrlScala.userInfoYunkai2Model(user)
          })
          val node = formatter.formatNode(usersList).asInstanceOf[ArrayNode]
          Utils.status(node.toString).toScala
        })
        ) rescue {
          case _: NotFoundException =>
            Future {
              Utils.status((new ObjectMapper().createArrayNode()).toString).toScala
            }
        }
      }
  }

  /**
   * 取得用户的群组信息
   *
   * @return
   */
  def getUserGroups(uid: Long, page: Int, pageSize: Int) = Action.async {
    request =>
      {
        val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
        (FinagleFactory.client.getUserChatGroups(uid, Some(basicChatGroupFieds), Some(page), Some(pageSize)) map (chatGroups => {
          val groupList: Seq[ChatGroup] = chatGroups map (chatGroup => {
            GroupCtrlScala.groupInfoYunkai2Model(chatGroup)
          })
          val node = formatter.formatNode(groupList).asInstanceOf[ArrayNode]
          Utils.status(node.toString).toScala
        })) rescue {
          case _: NotFoundException =>
            Future {
              Utils.status((new ObjectMapper().createArrayNode()).toString).toScala
            }
        }
      }
  }

  /**
   * 操作群组
   *
   * @param gid
   * @return
   */
  def opGroup(gid: Long) = Action.async {
    request =>
      {
        object ActionCode extends Enumeration {
          val ADD_MEMBER = Value(1)
          val DELETE_MEMBER = Value(2)
        }
        val operator = (request.headers.get("UserId") map (_.toLong)).get
        val jsonNode = request.body.asJson.get
        val action = (jsonNode \ "action").asOpt[Int].get
        val participants = (jsonNode \ "members").asOpt[Array[Long]].get
        Future {
          action match {
            case item if item == ActionCode.ADD_MEMBER.id =>
              FinagleFactory.client.addChatGroupMembers(gid, operator, participants)
              Utils.createResponse(ErrorCode.NORMAL, "Success").toScala
            case item if item == ActionCode.DELETE_MEMBER.id =>
              FinagleFactory.client.removeChatGroupMembers(gid, operator, participants)
              Utils.createResponse(ErrorCode.NORMAL, "Success").toScala
            case _ =>
              Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
        } rescue {
          case _: NotFoundException =>
            Future {
              Utils.createResponse(ErrorCode.DATA_NOT_EXIST).toScala
            }
        }
      }
  }

  def addChatGroupMember(gid: Long) = Action.async(request => {
    val operator = (request.headers.get("UserId") map (_.toLong)).get
    val future = for {
      body <- request.body.asJson
      member <- (body \ "member").asOpt[Long]
    } yield {
      FinagleFactory.client.addChatGroupMembers(gid, operator, Seq(member)) map (_ => {
        Utils.createResponse(ErrorCode.NORMAL).toScala
      }) rescue {
        case _: NotFoundException =>
          TwitterFuture {
            Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
          }
      }
    }

    val ret = future getOrElse TwitterFuture {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }

    ret

  })

  def deleteChatGroupMember(groupId: Long, memberId: Long) = Action.async(request => {
    val client = FinagleFactory.client

    val operator = (request.headers.get("UserId") map (_.toLong)).get
    val futureGroup = client.getChatGroup(groupId, None)
    val futureUser = client.getUserById(memberId, None, None)

    val future = (for {
      _ <- futureGroup
      _ <- futureUser
      ret <- client.removeChatGroupMembers(groupId, operator, Seq(memberId))
    } yield {
      Utils.createResponse(ErrorCode.NORMAL).toScala
    }) rescue {
      case _: NotFoundException =>
        TwitterFuture {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
        }
    }

    future
  })

  // 讨论组搜索, 后面再做
  def searchChatGroups(query: String, tags: String, visible: Boolean, page: Int, pageSize: Int) = Action.async(request => {
    Future {
      Utils.createResponse(ErrorCode.INVALID_ARGUMENT).toScala
    }
  })
}
