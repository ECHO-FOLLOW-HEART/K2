package aizou.core

import java.util.regex.Pattern

import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{ ObjectMapper, JsonNode }
import database.MorphiaFactory
import exception.{ AizouException, ErrorCode }
import misc.EsFactory
import models.AizouBaseEntity
import models.geo.Locality
import org.bson.types.ObjectId
import org.elasticsearch.common.lucene.search.function.{ CombineFunction, FieldValueFactorFunction }
import org.elasticsearch.index.query.{ FilterBuilders, QueryBuilders }
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.collection.JavaConversions._
import scala.concurrent.{ Future => ScalaFuture }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/9/29.
 */
object GeoAPIScala {

  val client = EsFactory.client
  case class Image(h: Int, key: String, w: Int)
  def searchLocality(keyword: String): ScalaFuture[ArrayNode] = ScalaFuture {
    val response = client.prepareSearch("locality")
      .setTypes("locality")
      .setQuery(QueryBuilders.multiMatchQuery(keyword, "country.zhName", "superAdm.zhName", "alias", "miscInfo.desc", "desc", "zhName"))
      //      .addFields("_id", "zhName", "images")
      .execute().actionGet()

    val hits = response.getHits
    val localityArrayNode = new ObjectMapper().createArrayNode()
    val result = for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield {
      val jsonRes = Json.parse(hit.getSourceAsString)

      implicit val imageReads: Reads[Image] = (
        (JsPath \ "h").read[Int] and
        (JsPath \ "key").read[String] and
        (JsPath \ "w").read[Int]
      )(Image.apply _)

      val node = new ObjectMapper().createObjectNode()
      node.put("id", hit.getId)
      node.put("zhName", (jsonRes \ "zhName").asOpt[String] getOrElse "")

      val imagesOpt = (JsPath \ "images").read[Seq[Image]].reads(jsonRes).asOpt
      val imagesArrayNode = new ObjectMapper().createArrayNode()
      imagesOpt getOrElse Seq() map (image => {
        val imageNode = new ObjectMapper().createObjectNode()
        imageNode.put("h", image.h)
        imageNode.put("w", image.w)
        imageNode.put("key", image.key)
        imagesArrayNode.add(imageNode)
      })
      node.set("images", imagesArrayNode)
      node
    }
    for (localityNode <- result)
      localityArrayNode.add(localityNode)
    localityArrayNode
  }

  def searchTravelNote(keyword: String): ScalaFuture[ArrayNode] = ScalaFuture {

    val response = client.prepareSearch("mongo_travelnote")
      .setTypes("travelnote")
      .setQuery(QueryBuilders.multiMatchQuery(keyword, "title", "contents"))
      //      .addFields("_id", "authorAvatar", "authorName", "title", "summary", "publishTime", "images", "detailUrl")
      .execute().actionGet()

    val hits = response.getHits
    val travelnoteArrayNode = new ObjectMapper().createArrayNode()
    val result = for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield {
      val jsonRes = Json.parse(hit.getSourceAsString)

      implicit val imageReads: Reads[Image] = (
        (JsPath \ "h").read[Int] and
        (JsPath \ "key").read[String] and
        (JsPath \ "w").read[Int]
      )(Image.apply _)

      val node = new ObjectMapper().createObjectNode()
      node.put("id", hit.getId)
      node.put("authorAvatar", (jsonRes \ "authorAvatar").asOpt[String] getOrElse "")
      node.put("authorName", (jsonRes \ "authorName").asOpt[String] getOrElse "")
      node.put("title", (jsonRes \ "title").asOpt[String] getOrElse "")
      node.put("summary", (jsonRes \ "summary").asOpt[String] getOrElse "")
      node.set("publishTime", (jsonRes \ "publishTime").asOpt[Long] map LongNode.valueOf getOrElse NullNode.getInstance())
      val imagesOpt = (JsPath \ "images").read[Seq[Image]].reads(jsonRes).asOpt
      val imagesArrayNode = new ObjectMapper().createArrayNode()
      imagesOpt getOrElse Seq() map (image => {
        val imageNode = new ObjectMapper().createObjectNode()
        imageNode.put("h", image.h)
        imageNode.put("w", image.w)
        imageNode.put("key", image.key)
        imagesArrayNode.add(imageNode)
      })
      node.set("images", imagesArrayNode)
      node.put("detailUrl", (jsonRes \ "detailUrl").asOpt[String] getOrElse "")
      node
    }
    for (travelnoteNode <- result)
      travelnoteArrayNode.add(travelnoteNode)
    travelnoteArrayNode
  }

  def searchViewspot(keyword: String): ScalaFuture[ArrayNode] = ScalaFuture {
    val response = client.prepareSearch("viewspot")
      .setTypes("viewspot")
      .setQuery(QueryBuilders.multiMatchQuery(keyword, "zhName", "name", "locality.zhName", "country.zhName", "address"))
      //      .addFields("_id", "zhName", "images", "rating", "address")
      .execute().actionGet()
    val hits = response.getHits
    val viewspotArrayNode = new ObjectMapper().createArrayNode()
    for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield {
      val jsonRes = Json.parse(hit.getSourceAsString)

      implicit val imageReads: Reads[Image] = (
        (JsPath \ "h").read[Int] and
        (JsPath \ "key").read[String] and
        (JsPath \ "w").read[Int]
      )(Image.apply _)

      val node = new ObjectMapper().createObjectNode()
      node.put("id", hit.getId)
      node.put("zhName", (jsonRes \ "zhName").asOpt[String] getOrElse "")

      val imagesOpt = (JsPath \ "images").read[Seq[Image]].reads(jsonRes).asOpt
      val imagesArrayNode = new ObjectMapper().createArrayNode()
      imagesOpt getOrElse Seq() map (image => {
        val imageNode = new ObjectMapper().createObjectNode()
        imageNode.put("h", image.h)
        imageNode.put("w", image.w)
        imageNode.put("key", image.key)
        imagesArrayNode.add(imageNode)
      })
      node.set("images", imagesArrayNode)
      node.put("rating", (jsonRes \ "rating").asOpt[Double] getOrElse 0.0)
      node.put("address", (jsonRes \ "address").asOpt[String] getOrElse "")
      viewspotArrayNode.add(node)
    }
    viewspotArrayNode
  }
  def searchRestaurant(keyword: String): ScalaFuture[ArrayNode] = ScalaFuture {
    val response = client.prepareSearch("restaurant")
      .setTypes("restaurant")
      .setQuery(QueryBuilders.multiMatchQuery(keyword, "style", "alias", "zhName", "address"))
      //      .addFields("_id", "zhName", "images", "rating", "address", "style")
      .execute().actionGet()
    val hits = response.getHits
    val restaurantArrayNode = new ObjectMapper().createArrayNode()
    for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield {
      val jsonRes = Json.parse(hit.getSourceAsString)

      implicit val imageReads: Reads[Image] = (
        (JsPath \ "h").read[Int] and
        (JsPath \ "key").read[String] and
        (JsPath \ "w").read[Int]
      )(Image.apply _)

      val node = new ObjectMapper().createObjectNode()
      node.put("id", hit.getId)
      node.put("zhName", (jsonRes \ "zhName").asOpt[String] getOrElse "")

      val imagesOpt = (JsPath \ "images").read[Seq[Image]].reads(jsonRes).asOpt
      val imagesArrayNode = new ObjectMapper().createArrayNode()
      imagesOpt getOrElse Seq() map (image => {
        val imageNode = new ObjectMapper().createObjectNode()
        imageNode.put("h", image.h)
        imageNode.put("w", image.w)
        imageNode.put("key", image.key)
        imagesArrayNode.add(imageNode)
      })
      node.set("images", imagesArrayNode)
      node.put("rating", (jsonRes \ "rating").asOpt[Double] getOrElse 0.0)
      node.put("address", (jsonRes \ "address").asOpt[String] getOrElse "")
      node.put("style", (jsonRes \ "style").asOpt[String] getOrElse "")
      restaurantArrayNode.add(node)
    }
    restaurantArrayNode
  }
  def searchShopping(keyword: String): ScalaFuture[ArrayNode] = ScalaFuture {
    val response = client.prepareSearch("shopping")
      .setTypes("shopping")
      .setQuery(QueryBuilders.multiMatchQuery(keyword, "style", "alias", "country.zhName", "locality.zhName", "zhName", "address", "tags"))
      //      .addFields("_id", "zhName", "images", "rating", "address", "style")
      .execute().actionGet()
    val hits = response.getHits
    val shoppingArrayNode = new ObjectMapper().createArrayNode()
    for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield {
      val jsonRes = Json.parse(hit.getSourceAsString)

      implicit val imageReads: Reads[Image] = (
        (JsPath \ "h").read[Int] and
        (JsPath \ "key").read[String] and
        (JsPath \ "w").read[Int]
      )(Image.apply _)

      val node = new ObjectMapper().createObjectNode()
      node.put("id", hit.getId)
      node.put("zhName", (jsonRes \ "zhName").asOpt[String] getOrElse "")

      val imagesOpt = (JsPath \ "images").read[Seq[Image]].reads(jsonRes).asOpt
      val imagesArrayNode = new ObjectMapper().createArrayNode()
      imagesOpt getOrElse Seq() map (image => {
        val imageNode = new ObjectMapper().createObjectNode()
        imageNode.put("h", image.h)
        imageNode.put("w", image.w)
        imageNode.put("key", image.key)
        imagesArrayNode.add(imageNode)
      })
      node.set("images", imagesArrayNode)
      node.put("rating", (jsonRes \ "rating").asOpt[Double] getOrElse 0.0)
      node.put("address", (jsonRes \ "address").asOpt[String] getOrElse "")
      node.put("style", (jsonRes \ "style").asOpt[String] getOrElse "")
      shoppingArrayNode.add(node)
    }
    shoppingArrayNode
  }

  // and
  //          (JsPath \ "cropHint").read[CropHint]
  //  case class CropHint(top: Int, right: String, left: Int, bottom: Int)
  //        implicit val cropHintReads: Reads[CropHint] = (
  //          (JsPath \ "top").read[Int] and
  //          (JsPath \ "right").read[String] and
  //          (JsPath \ "left").read[Int] and
  //          (JsPath \ "bottom").read[Int]
  //        )(CropHint.apply _)
  //, cropHint: CropHint)
  //  case class Locality1(images: Seq[Image], alias: Seq[String], hotness: Double, id: String, zhName: String)
  //        implicit val Locality1Reads: Reads[Locality1] = (
  //          (JsPath \ "images").read[Seq[Image]] and
  //          (JsPath \ "alias").read[Seq[String]] and
  //          (JsPath \ "hotness").read[Double] and
  //          (JsPath \ "_id").read[String] and
  //          (JsPath \ "zhName").read[String]
  //        )(Locality1.apply _)
  //        jsonRes.validate[Locality1] match {
  //          case s: JsSuccess[Locality1] =>
  //            val locality1 = s.get
  //            val h = locality1.images.get(0).h
  //            val w = locality1.images.get(0).w
  //          case e: JsError =>
  //            val result = "error"
  //            val result1 = "error"
  //        }

  //  def searchMultimatchFunction: Future[Seq[String]] = Future {
  //    val response = client.prepareSearch("es_qa").setTypes("qa")
  //      .setQuery(QueryBuilders.multiMatchQuery("quick", "title", "contents"))
  //      .setQuery(QueryBuilders.functionScoreQuery(
  //        ScoreFunctionBuilders.gaussDecayFunction("vote_cnt", "11", "2").setDecay(0.2).setOffset("4")
  //      ).add(ScoreFunctionBuilders.fieldValueFactorFunction("qid").modifier(FieldValueFactorFunction.Modifier.SQRT))
  //        .scoreMode("multiply"))
  //      .execute().actionGet()
  //
  //    val hits = response.getHits
  //    // println(hits.getHits)
  //    val result = for {
  //      hit <- hits.getHits
  //    } yield {
  //      hit.getSourceAsString
  //      // val node = new ObjectMapper().createObjectNode()
  //      //      val ret = Json.parse(str)
  //    }
  //    result.toSeq
  //  }
  //
  //  def searchMultimatchBoost: Future[Seq[String]] = Future {
  //    val response = client.prepareSearch("es_qa")
  //      .setTypes("qa")
  //      // 在'title'和'contents'中搜索'quick'
  //      .setQuery(QueryBuilders.multiMatchQuery("quick", "title", "contents^15"))
  //      .execute().actionGet()
  //
  //    val hits = response.getHits
  //    // println(hits.getHits)
  //    val result = for {
  //      hit <- hits.getHits
  //    } yield // 貌似没有能输入全部内容的方法，只能一个一个来凑
  //    hit.getSourceAsString
  //
  //    result.toSeq
  //  }
  //
  //  def searchFilter: Future[Seq[String]] = Future {
  //    val response = client.prepareSearch("es_qa")
  //      .setTypes("qa")
  //      .setQuery(QueryBuilders.filteredQuery(QueryBuilders.multiMatchQuery("时间", "title", "contents"),
  //        FilterBuilders.termFilter("type", "answer")))
  //      .execute()
  //      .actionGet()
  //    val hits = response.getHits
  //    // println(hits.getHits)
  //    val result = for {
  //      hit <- hits.getHits
  //    } yield {
  //      hit.getSourceAsString
  //    }
  //    result.toSeq
  //  }

  def searchLocalities(keyword: String, prefix: String, countryId: ObjectId,
    page: Int, pageSize: Int, fields: Set[String]): Seq[Locality] = {

    val ds = MorphiaFactory.datastore
    val query = ds.createQuery(classOf[Locality])
    if (keyword != null)
      query.field("alias").equal(Pattern.compile("^" + keyword))

    if (countryId != null)
      query.field(Locality.fnCountry + ".id").equal(countryId)
    query.field(AizouBaseEntity.FD_TAOZIENA).equal(true)

    if (fields != null && !fields.isEmpty)
      query.retrievedFields(true, fields.toSeq: _*)
    val result = query.order("-" + Locality.fnHotness)
      .offset(page * pageSize).limit(pageSize).asList()

    result.toSeq
  }
}
