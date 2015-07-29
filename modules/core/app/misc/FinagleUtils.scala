package misc

import com.lvxingpai.yunkai.UserInfoProp

/**
 * Created by topy on 2015/7/29.
 */
object FinagleUtils {

  def updateUserAvatar(userId: String, url: String) {
    val map: Map[UserInfoProp, String] = Map(UserInfoProp.Avatar -> url)
    FinagleFactory.client.updateUserInfo(userId.toLong, map)
  }
}
