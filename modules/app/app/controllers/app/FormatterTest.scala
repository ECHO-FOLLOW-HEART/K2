package controllers.app

import scala.concurrent.Future
import formatter.taozi.geo.CountryExpertFormatterScala
import models.geo.{ Continent, CountryExpert }
import models.misc.ImageItem
import org.bson.types.ObjectId
import play.api.mvc.{ Controller, Action }
import utils.Utils
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/8/28.
 */
object FormatterTest extends Controller {

  def bar() = Action.async(
    request => {
      val formatter = new CountryExpertFormatterScala()
      val expert = new CountryExpert()
      expert.setId(new ObjectId())
      expert.setCode("10000")

      val continent = new Continent()
      continent.setCode("100")
      continent.setEnName("ouzhou")
      continent.setZhName("欧洲")
      continent.setId(new ObjectId())
      expert.setContinent(continent)

      expert.setEnName("Chinese")
      expert.setZhName("中国")
      expert.setExpertCnt(Integer.valueOf(10))

      val image = new ImageItem()
      image.setH(Integer.valueOf(100))
      image.setW(Integer.valueOf(200))
      val images = Seq(image)

      expert.setImages(seqAsJavaList(images))
      val node = formatter.formatJsonNode(expert)
      val result = Utils.status(node.toString).toScala
      Future { result }
    })

}
