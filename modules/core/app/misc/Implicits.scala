package misc

import com.lvxingpai.yunkai.{UserInfo => YunkaiUserInfo}
import models.user.UserInfo

import scala.language.implicitConversions

/**
 * Created by zephyre on 6/30/15.
 */
object Implicits {
  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  implicit def resultJava2Scala(result: play.mvc.Result): play.api.mvc.Result = result.toScala
}
