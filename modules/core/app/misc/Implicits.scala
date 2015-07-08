package misc

import com.lvxingpai.yunkai.{ OperationCode, UserInfo => YunkaiUserInfo }
import models.user.UserInfo

import scala.language.implicitConversions

/**
 * Created by zephyre on 6/30/15.
 */
object Implicits {

  implicit def userInfoYunkai2Model(userInfo: YunkaiUserInfo): UserInfo = FinagleConvert.convertK2User(userInfo)

  implicit def resultJava2Scala(result: play.mvc.Result): play.api.mvc.Result = result.toScala

  implicit def int2OperationCode(action: Int): OperationCode = {
    import OperationCode._

    action match {
      case item if item == Signup.value => Signup
      case item if item == ResetPassword.value => ResetPassword
      case item if item == UpdateTel.value => UpdateTel
    }
  }
}
