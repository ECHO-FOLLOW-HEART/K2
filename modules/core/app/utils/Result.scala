package utils

import com.fasterxml.jackson.databind.node.{IntNode, LongNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import exception.ErrorCode
import play.api.mvc.Results

import scala.language.postfixOps

/**
 * 生成K2的Result
 *
 * Created by zephyre on 7/6/15.
 */
object Result {

  import play.api.http.Status._

  def apply(statusCode: Int = 200, retCode: ErrorCode = ErrorCode.NORMAL, resultNode: Option[JsonNode] = None): play.api.mvc.Result = {
    val mapper = new ObjectMapper()
    val node = mapper.createObjectNode()
    node.set("lastModified", LongNode.valueOf(System.currentTimeMillis / 1000))
    node.set("code", IntNode.valueOf(retCode.getVal))
    if (resultNode nonEmpty)
      node.set("result", resultNode.get)

    val contents = mapper.writeValueAsString(node)
    Results.Status(statusCode)(contents).as("application/json;charset=utf-8")
  }

  def apply(statusCode: Int, retCode: ErrorCode, message: String): play.api.mvc.Result = {
    val mapper = new ObjectMapper()
    val node = mapper.createObjectNode()
    node.set("lastModified", LongNode.valueOf(System.currentTimeMillis / 1000))
    node.set("code", IntNode.valueOf(retCode.getVal))
    node.put("message", message)

    val contents = mapper.writeValueAsString(node)
    Results.Status(statusCode)(contents).as("application/json;charset=utf-8")
  }

  def ok = Result(OK, ErrorCode.NORMAL, _: Option[JsonNode])

  def created = Result(CREATED, ErrorCode.NORMAL, _: Option[JsonNode])

  def conflict = Result(CONFLICT, _: ErrorCode, _: String)

  def notFound = Result(NOT_FOUND, _: ErrorCode, _: String)

  def badRequest = Result(BAD_REQUEST, _: ErrorCode, _: String)

  def forbidden = Result(FORBIDDEN, _: ErrorCode, _: String)

  def unauthorized = Result(UNAUTHORIZED, _: ErrorCode, _: String)

  val unprocessable = Result(UNPROCESSABLE_ENTITY, ErrorCode.INVALID_ARGUMENT)
}
