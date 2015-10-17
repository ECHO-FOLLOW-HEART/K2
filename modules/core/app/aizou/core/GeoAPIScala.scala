package aizou.core

import java.util.regex.Pattern

import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.ObjectMapper
import database.MorphiaFactory
import formatter.taozi.geo.SearchLocalityParserScala
import formatter.taozi.misc.SearchTravelNoteParserScala
import formatter.taozi.poi.{ SearchShoppingParserScala, SearchRestaurantParserScala, SearchViewSpotParserScala }

import misc.EsFactory
import models.AizouBaseEntity
import models.geo.Locality
import models.misc.TravelNoteScala
import models.poi.{ Restaurant, Shopping, ViewSpot }
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
  def searchLocality(keyword: String, locality: Boolean, page: Int, pageSize: Int): ScalaFuture[Seq[Locality]] = ScalaFuture {
    if (locality) {
      val response = client.prepareSearch("locality")
        .setTypes("locality")
        .setQuery(
          QueryBuilders.functionScoreQuery(
            QueryBuilders.boolQuery()
              .should(QueryBuilders.matchQuery("alias", keyword))
              .should(QueryBuilders.matchQuery("zhName", keyword)),
            ScoreFunctionBuilders.fieldValueFactorFunction("rating").missing(0.5).factor(10)
          ).boostMode(CombineFunction.MULT)
        )
        .setSize(pageSize) // 一次返回n个
        .setFrom(pageSize * (page - 1)) // 从跳过n个开始
        .execute().actionGet()
      val hits = response.getHits
      val results = for {
        //只显示搜索结果，要显示相关的_score,需要再调用score方法
        hit <- hits.getHits
      } yield SearchLocalityParserScala(hit.getSourceAsString)
      results.toSeq
    } else Seq()
  }

  def searchTravelNote(keyword: String, page: Int, pageSize: Int): ScalaFuture[Seq[TravelNoteScala]] = ScalaFuture {
    val response = client.prepareSearch("mongo_travelnote")
      .setTypes("travelnote")
      .setQuery(
        QueryBuilders.functionScoreQuery(
          QueryBuilders.nestedQuery("contents",
            QueryBuilders.boolQuery()
              .should(QueryBuilders.matchQuery("contents.title", keyword).boost(5f))
              .should(QueryBuilders.matchQuery("contents.content", keyword).boost(1f))),
          ScoreFunctionBuilders.fieldValueFactorFunction("rating").missing(0.5))
          .boostMode(CombineFunction.MULT))
      //.addPartialField("data", includes , null)
      .setSize(pageSize) // 一次返回n个
      .setFrom(pageSize * (page - 1)) // 从跳过n个开始
      .execute().actionGet()

    val hits = response.getHits
    val travelnoteArrayNode = new ObjectMapper().createArrayNode()
    val results = for {
      //只显示搜索结果，要显示相关的_score,需要再调用score方法
      hit <- hits.getHits
    } yield SearchTravelNoteParserScala(hit.getSourceAsString)
    results.toSeq
  }

  def searchViewspot(keyword: String, viewspot: Boolean, page: Int, pageSize: Int): ScalaFuture[Seq[ViewSpot]] = ScalaFuture {

    if (viewspot) {
      val response = client.prepareSearch("viewspot")
        .setTypes("viewspot")
        .setQuery(
          QueryBuilders.functionScoreQuery(
            QueryBuilders.boolQuery()
              .should(QueryBuilders.matchQuery("alias", keyword))
              .should(QueryBuilders.matchQuery("zhName", keyword))
              .should(QueryBuilders.nestedQuery("locality",
                QueryBuilders.boolQuery()
                  .should(QueryBuilders.matchQuery("locality.enName", keyword))
                  .should(QueryBuilders.matchQuery("locality.zhName", keyword))
              )),
            ScoreFunctionBuilders.fieldValueFactorFunction("rating").missing(0.5).factor(10)
          ).boostMode(CombineFunction.MULT)
        )
        .setSize(pageSize) // 一次返回n个
        .setFrom(pageSize * (page - 1)) // 从跳过n个开始
        .execute().actionGet()
      val hits = response.getHits

      val results = for {
        //只显示搜索结果，要显示相关的_score,需要再调用score方法
        hit <- hits.getHits
      } yield SearchViewSpotParserScala(hit.getSourceAsString)
      results.toSeq
    } else Seq()
  }

  def searchRestaurant(keyword: String, restaurant: Boolean, page: Int, pageSize: Int): ScalaFuture[Seq[Restaurant]] = ScalaFuture {
    if (restaurant) {
      val response = client.prepareSearch("restaurant")
        .setTypes("restaurant")
        .setQuery(
          QueryBuilders.functionScoreQuery(
            QueryBuilders.boolQuery()
              .should(QueryBuilders.matchQuery("alias", keyword))
              .should(QueryBuilders.matchQuery("zhName", keyword))
              .should(QueryBuilders.nestedQuery("locality",
                QueryBuilders.boolQuery()
                  .should(QueryBuilders.matchQuery("locality.enName", keyword))
                  .should(QueryBuilders.matchQuery("locality.zhName", keyword))
              )),
            ScoreFunctionBuilders.fieldValueFactorFunction("rating").missing(0.5).factor(10)
          ).boostMode(CombineFunction.MULT)
        )
        .setSize(pageSize) // 一次返回n个
        .setFrom(pageSize * (page - 1)) // 从跳过n个开始
        .execute().actionGet()
      val hits = response.getHits

      val results = for {
        //只显示搜索结果，要显示相关的_score,需要再调用score方法
        hit <- hits.getHits
      } yield SearchRestaurantParserScala(hit.getSourceAsString)
      results.toSeq
    } else Seq()
  }

  def searchShopping(keyword: String, shopping: Boolean, page: Int, pageSize: Int): ScalaFuture[Seq[Shopping]] = ScalaFuture {
    if (shopping) {
      val response = client.prepareSearch("shopping")
        .setTypes("shopping")
        .setQuery(
          QueryBuilders.functionScoreQuery(
            QueryBuilders.boolQuery()
              .should(QueryBuilders.matchQuery("alias", keyword))
              .should(QueryBuilders.matchQuery("zhName", keyword))
              .should(QueryBuilders.nestedQuery("locality",
                QueryBuilders.boolQuery()
                  .should(QueryBuilders.matchQuery("locality.enName", keyword))
                  .should(QueryBuilders.matchQuery("locality.zhName", keyword))
              )),
            ScoreFunctionBuilders.fieldValueFactorFunction("rating").missing(0.5).factor(10)
          ).boostMode(CombineFunction.MULT)
        )
        .setSize(pageSize) // 一次返回n个
        .setFrom(pageSize * (page - 1)) // 从跳过n个开始
        .execute().actionGet()
      val hits = response.getHits

      val results = for {
        //只显示搜索结果，要显示相关的_score,需要再调用score方法
        hit <- hits.getHits
      } yield SearchShoppingParserScala(hit.getSourceAsString)
      results.toSeq
    } else Seq()
  }

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
