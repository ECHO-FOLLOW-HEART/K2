package models.misc

import java.util.{ List => JList }

import models.geo.Locality
import models.poi.ViewSpot
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Id, Entity }

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/10/13.
 */
@Entity
class TravelNoteScala {

  @BeanProperty
  @Id
  var id: ObjectId = new ObjectId()

  /**
   * 名称(与Title名称一致)
   */
  @BeanProperty
  var name: String = ""
  /**
   * 游记标题
   */
  @BeanProperty
  var title: String = null

  /**
   * 作者名称
   */
  @BeanProperty
  var author: String = null
  @BeanProperty
  var authorName: String = null
  /**
   * 作者头像
   */
  @BeanProperty
  var avatar: String = null
  @BeanProperty
  var authorAvatar: String = null

  /**
   * 作者的id
   */
  @BeanProperty
  var authorId: Long = 0

  /**
   * 发表时间
   */
  @BeanProperty
  var publishTime: Long = 0

  /**
   * 发表时间
   */
  @BeanProperty
  var startDate: Long = 0
  /**
   * 收藏次数
   */
  @BeanProperty
  var favorCnt: Int = 0

  /**
   * 评论次数
   */
  @BeanProperty
  var commentCnt: Int = 0

  /**
   * 浏览次数
   */
  @BeanProperty
  var viewCnt: Int = 0

  /**
   * 分享次数
   */
  @BeanProperty
  var shareCnt: Int = 0
  /**
   * 评分
   */
  @BeanProperty
  var rating: Double = 0.5
  /**
   * 热度
   */
  @BeanProperty
  var hotness: Double = 0.5

  /**
   * 游记中提到的景点
   */
  @BeanProperty
  var viewSpotList: JList[ViewSpot] = null

  /**
   * 游记中提到的目的地
   */
  @BeanProperty
  var localityList: JList[Locality] = null

  /**
   * 最小天数
   */
  @BeanProperty
  var lowerDays: Int = 0

  /**
   * 最大天数
   */
  @BeanProperty
  var uppperDays: Int = 0

  /**
   * 人均花销
   */
  @BeanProperty
  var lowerCost: Double = 0.0
  @BeanProperty
  var upperCost: Double = 0.0

  /**
   * 出游的月份/季节
   */
  @BeanProperty
  var months: JList[Int] = null

  /**
   * 出游的时间
   */
  @BeanProperty
  var travelTime: Long = 0

  /**
   * 花费下限
   */
  @BeanProperty
  var costLower: Float = 0.0f

  /**
   * 花费上限
   */
  @BeanProperty
  var costUpper: Float = 0.0f

  /**
   * 旅行开支
   */
  @BeanProperty
  var costNorm: Float = 0.0f

  /**
   * 旅行天数
   */
  @BeanProperty
  var days: Int = 0

  /**
   * 出发地
   */
  @BeanProperty
  var fromLoc: String = null

  /**
   * 目的地
   */
  @BeanProperty
  var toLoc: JList[String] = null

  /**
   * 游记摘要
   */
  @BeanProperty
  var summary: String = null

  /**
   * 游记正文
   */
  @BeanProperty
  var contentsList: JList[String] = null
  @BeanProperty
  var contents: JList[Map[String, String]] = null

  /**
   * 游记标签
   */
  @BeanProperty
  var tags: JList[String] = null
  /**
   * 游记正文
   */
  @BeanProperty
  var content: String = null
  /**
   * 游记来源
   */
  @BeanProperty
  var source: String = null

  /**
   * 游记原始网址
   */
  @BeanProperty
  var sourceUrl: String = null

  /**
   * 是否为精华游记
   */
  @BeanProperty
  var elite: Boolean = false
  @BeanProperty
  var essence: Boolean = false
  /**
   * 图像
   */
  @BeanProperty
  var images: JList[ImageItem] = null
  @BeanProperty
  var cover: String = ""

  //  @BeanProperty
  //  var detailUrl: String = ""
}

object TravelNoteScala {

  val fnId = "id"
  val fnName = "name"
  val fnTitle = "title"
  val fnDesc = "desc"
  val fnCover = "cover"
  val fnCovers = "covers"
  val fnImages = "images"
  val fnAuthorName = "authorName"
  val fnAuthorAvatar = "authorAvatar"
  val fnSource = "source"
  val fnSourceUrl = "sourceUrl"
  val fnPublishTime = "publishTime"
  val fnTravelTime = "travelTime"
  val fnStartDate = "startDate"
  val fnSummary = "summary"
  val fnContents = "contentsList"
  val fnNoteContents = "contents"
  val fnCostLower = "costLower"
  val fnLowerCost = "lowerCost"
  val fnCostUpper = "costUpper"
  val fnUpperCost = "upperCost"
  val fnFavorCnt = "favorCnt"
  val fnCommentCnt = "commentCnt"
  val fnViewCnt = "viewCnt"
  val fnRating = "rating"
  val fnEssence = "essence"
}