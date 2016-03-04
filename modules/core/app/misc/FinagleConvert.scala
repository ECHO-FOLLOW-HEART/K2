package misc

import com.lvxingpai.yunkai.Gender
import com.lvxingpai.yunkai.{ UserInfo => YunkaiUserInfo, ChatGroup => YunkaiChatGroup }
import models.group.ChatGroup
import models.user.UserInfo
import org.bson.types.ObjectId
import org.joda.time.format.DateTimeFormat
import scala.language.postfixOps

/**
 * Created by zephyre on 6/30/15.
 */
object FinagleConvert {
  def convertK2User(userInfo: YunkaiUserInfo): UserInfo = {
    val result = new UserInfo()
    result.setId(new ObjectId(userInfo.id))
    result.setUserId(userInfo.userId)
    result.setNickName(userInfo.nickName)
    result.setAvatar(userInfo.avatar getOrElse "")
    result.setGender(userInfo.gender map convertUserGender getOrElse "")
    result.setSignature(userInfo.signature getOrElse "")
    result.setTel(userInfo.tel getOrElse "")
    result.setMemo(userInfo.memo getOrElse "")
    result.setResidence(userInfo.residence getOrElse "")
    result.setBirthday(userInfo.birth map (input => {
      // 日期格式转换。将yyyy-MM-dd格式，转换为MM/dd/yyyy格式
      val fmtInput = DateTimeFormat.forPattern("MM/dd/yyyy")
      fmtInput.parseDateTime(input).toDate
    }) orNull)
    result.setPromotionCode(userInfo.promotionCode)
    result
  }

  def convertK2ChatGroup(chatGroup: YunkaiChatGroup): ChatGroup = {
    val result: ChatGroup = new ChatGroup
    result.setId(new ObjectId(chatGroup.id))
    result.setGroupId(chatGroup.chatGroupId)
    result.setCreator(chatGroup.creator)
    result.setName(Option(chatGroup.name) getOrElse "")
    result.setDesc(chatGroup.groupDesc getOrElse "")
    result.setAvatar(chatGroup.avatar getOrElse "")
    result.setMaxUsers(chatGroup.maxUsers)
    result
  }

  def convertUserGender(value: Gender): String = {
    if (Gender.Male.value == value.value) return models.user.UserInfo.fnGender_M
    else if (Gender.Female.value == value.value) return models.user.UserInfo.fnGender_F
    else if (Gender.Secret.value == value.value) return models.user.UserInfo.fnGender_S
    else if (Gender.Both.value == value.value) return models.user.UserInfo.fnGender_B
    else return models.user.UserInfo.fnGender_None
  }

}
