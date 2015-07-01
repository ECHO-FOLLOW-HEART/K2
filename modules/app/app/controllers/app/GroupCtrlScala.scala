package controllers.app

import com.fasterxml.jackson.databind.{ObjectMapper, JsonNode}

import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.lvxingpai.yunkai.{ChatGroup => YunkaiChatGroup, ChatGroupProp, NotFoundException}
import com.lvxingpai.yunkai.{NotFoundException, UserInfo => YunkaiUserInfo, UserInfoProp}
import com.twitter.util.{Future => TwitterFuture}
import exception.ErrorCode
import formatter.FormatterFactory
import formatter.taozi.group.ChatGroupFormatter
import formatter.taozi.user.{UserInfoSimpleFormatter, UserInfoFormatter}
import misc.TwitterConverter._
import misc.{FinagleConvert, FinagleFactory}
import models.group.ChatGroup
import models.user.UserInfo
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller, Result}
import utils.Utils

import scala.concurrent.Future
import scala.language.{implicitConversions, postfixOps}

/**
 * Created by zephyre on 6/30/15.
 */
object GroupCtrlScala extends Controller {

  implicit def groupInfoYunkai2Model(groupInfo: YunkaiChatGroup): ChatGroup = FinagleConvert.convertK2ChatGroup(groupInfo)

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  val basicChatGroupFieds = Seq(ChatGroupProp.ChatGroupId, ChatGroupProp.Name, ChatGroupProp.Visible, ChatGroupProp.Avatar, ChatGroupProp.GroupDesc)
  val ACTION_ADDMEMBERS = "addMembers"
  val ACTION_DELMEMBERS = "delMembers"

  /**
   * 取得群组信息
   *
   * @param gID
   * @return
   */
  def getGroup(gID: Long) = Action.async {
    request => {
      val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
      val future: Future[Result] = try {
        FinagleFactory.client.getChatGroup(gID, Some(basicChatGroupFieds)) map (chatGroup => {
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


  /**
   * 创建群组
   *
   * @return
   */
  def createGroup() = Action.async {
    request => {
      val uid = request.headers.get("UserId").get.toLong
      val jsonNode = request.body.asJson.get

      val name = (jsonNode \ "name").asOpt[String].getOrElse("")
      val avatar = (jsonNode \ "avatar").asOpt[String].getOrElse("")
      val desc = (jsonNode \ "desc").asOpt[String].getOrElse("")
      val participants = (jsonNode \ "participants").asOpt[Array[Long]]
      val participantsValue = participants.get.toSeq
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
    request => {
      val uid = request.headers.get("UserId").get.toLong
      val jsonNode = request.body.asJson.get
      val groupId = (jsonNode \ "groupId").asOpt[Long].get
      val name = (jsonNode \ "name").asOpt[String]
      val desc = (jsonNode \ "desc").asOpt[String]
      val avatar = (jsonNode \ "avatar").asOpt[String]
      val participants = (jsonNode \ "participants").asOpt[Array[Long]]
      val participantsValue = participants.get.toSeq

      val propMap = scala.collection.mutable.Map[ChatGroupProp, String]()
      if (name.nonEmpty)
        propMap.put(ChatGroupProp.Name, name.get)
      if (desc.nonEmpty)
        propMap.put(ChatGroupProp.GroupDesc, desc.get)
      if (avatar.nonEmpty)
        propMap.put(ChatGroupProp.Avatar, avatar.get)

      val formatter = FormatterFactory.getInstance(classOf[ChatGroupFormatter])
      (FinagleFactory.client.updateChatGroup(uid, propMap) map (chatGroup => {
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
    request => {
      val formatter = FormatterFactory.getInstance(classOf[UserInfoSimpleFormatter])
      (FinagleFactory.client.getChatGroupMembers(gid, Some(UserCtrlScala.basicUserInfoFieds)) map (users => {
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
    request => {
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
    request => {
      val uid = request.headers.get("UserId").get.toLong
      val jsonNode = request.body.asJson.get
      val action = (jsonNode \ "action").asOpt[String].get
      val participants = (jsonNode \ "participants").asOpt[Array[Long]].get
      (Future {
        action match {
          case ACTION_ADDMEMBERS =>
            FinagleFactory.client.addChatGroupMembers(gid, uid, participants)
          case ACTION_DELMEMBERS =>
            FinagleFactory.client.removeChatGroupMembers(gid, uid, participants)
        }
        Utils.createResponse(ErrorCode.NORMAL, "Success").toScala
      }) rescue {
        case _: NotFoundException =>
          Future {
            Utils.createResponse(ErrorCode.DATA_NOT_EXIST).toScala
          }
      }
    }
  }
}
