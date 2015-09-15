package models.user

import java.util.{ List => JList }
import javax.validation.constraints.{ Size, Min, NotNull }

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Indexed, Entity }

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/8/26.
 */
@Entity
class UserProfile {
  @BeanProperty
  @NotNull
  @Min(value = 1)
  @Indexed(unique = true)
  var userId: Long = 0

  /**
   * 派派点评
   */
  @BeanProperty
  @Size(min = 2, max = 512)
  var profile: String = null

  /**
   * 达人标签
   */
  @BeanProperty
  var tags: JList[String] = null

  /**
   * 达人地域
   */
  @BeanProperty
  var zone: JList[ObjectId] = null
}

object UserProfile {
  val fdUserId = "userId"
  val fdProfile = "profile"
  val fdTags = "tags"
  val fdZone = "zone"

  def apply(userId: Long): UserProfile = {
    val result = new UserProfile
    result.userId = userId
    result
  }
}